package com.prakash.clinicos.queue.service;

import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.notification.service.NotificationService;
import com.prakash.clinicos.queue.dto.request.CheckInRequest;
import com.prakash.clinicos.queue.dto.request.GenerateTokenRequest;
import com.prakash.clinicos.queue.dto.response.QueueTokenResponse;
import com.prakash.clinicos.queue.entity.QueueStatus;
import com.prakash.clinicos.queue.entity.QueueToken;
import com.prakash.clinicos.queue.repository.QueueTokenRepository;
import com.prakash.clinicos.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class QueueService {

    /** Statuses that still allow queue operations (call, start, skip, cancel). */
    private static final Set<QueueStatus> TERMINAL = Set.of(
            QueueStatus.COMPLETED, QueueStatus.CANCELLED);

    private final QueueTokenRepository queueRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public QueueService(QueueTokenRepository queueRepository,
                        ClinicRepository clinicRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository,
                        AppointmentRepository appointmentRepository,
                        NotificationService notificationService,
                        SimpMessagingTemplate messagingTemplate) {
        this.queueRepository = queueRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Pushes the changed token to every screen subscribed to this clinic's queue board.
     * Called after every state transition so reception/doctor/display screens stay live
     * without polling.
     */
    private void broadcast(Long clinicId, QueueTokenResponse token) {
        messagingTemplate.convertAndSend("/topic/clinics/" + clinicId + "/queue", token);
    }

    // ── Generate token (walk-in) ──────────────────────────────────────────────

    /**
     * Issues a new queue token for a walk-in patient.
     *
     * Token numbers are sequential per doctor per day (1, 2, 3 …).
     * They reset to 1 every morning — this is handled automatically by
     * checking MAX(token_number) for today's date.
     */
    @Transactional
    public QueueTokenResponse generateToken(Long clinicId, GenerateTokenRequest req,
                                             UserPrincipal principal) {
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found: " + clinicId));

        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(req.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Doctor not found: " + req.getDoctorId()));
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Doctor does not belong to this clinic");
        }

        Patient patient = patientRepository.findByIdAndDeletedFalse(req.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Patient not found: " + req.getPatientId()));
        if (!patient.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Patient does not belong to this clinic");
        }

        LocalDate today = LocalDate.now();
        int tokenNumber = queueRepository.getNextTokenNumber(clinicId, doctor.getId(), today);

        QueueToken token = QueueToken.builder()
                .clinicId(clinicId)
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .tokenNumber(tokenNumber)
                .queueDate(today)
                .notes(req.getNotes())
                .createdBy(principal.getId())
                .build();

        token = queueRepository.save(token);
        log.info("Walk-in token generated: #{} for doctor={}, patient={}, clinic={}",
                tokenNumber, doctor.getId(), patient.getId(), clinicId);

        QueueTokenResponse result = toResponse(token, doctor.getFullName(), patientFullName(patient));
        broadcast(clinicId, result);
        return result;
    }

    // ── Check in (appointment) ────────────────────────────────────────────────

    /**
     * Checks in a patient who has a booked appointment.
     *
     * Why separate from generateToken?
     * - Appointment check-in links the token to an appointment (for history tracking).
     * - The doctor and patient are taken from the appointment — receptionist just
     *   scans/selects the appointment, not the patient and doctor separately.
     * - Prevents checking in an appointment that's already been cancelled or completed.
     */
    @Transactional
    public QueueTokenResponse checkIn(Long clinicId, CheckInRequest req,
                                       UserPrincipal principal) {
        Appointment appt = appointmentRepository.findByIdAndClinicId(
                        req.getAppointmentId(), clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Appointment not found: " + req.getAppointmentId()));

        // Only PENDING or CONFIRMED appointments can check in
        if (appt.getStatus() == AppointmentStatus.CANCELLED
                || appt.getStatus() == AppointmentStatus.COMPLETED
                || appt.getStatus() == AppointmentStatus.RESCHEDULED) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Cannot check in — appointment is " + appt.getStatus().name().toLowerCase());
        }

        // Prevent double check-in
        if (queueRepository.existsByAppointmentId(appt.getId())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Patient has already checked in for this appointment");
        }

        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(appt.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Patient patient = patientRepository.findByIdAndDeletedFalse(appt.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));

        LocalDate today = LocalDate.now();
        int tokenNumber = queueRepository.getNextTokenNumber(clinicId, doctor.getId(), today);

        QueueToken token = QueueToken.builder()
                .clinicId(clinicId)
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .appointmentId(appt.getId())
                .tokenNumber(tokenNumber)
                .queueDate(today)
                .notes(req.getNotes())
                .createdBy(principal.getId())
                .build();

        // Also mark the appointment as CONFIRMED (patient physically present)
        if (appt.getStatus() == AppointmentStatus.PENDING) {
            appt.setStatus(AppointmentStatus.CONFIRMED);
            appt.setConfirmedBy(principal.getId());
            appt.setConfirmedAt(LocalDateTime.now());
            appointmentRepository.save(appt);
        }

        token = queueRepository.save(token);
        log.info("Appointment check-in: token=#{}, appointment={}, patient={}, clinic={}",
                tokenNumber, appt.getId(), patient.getId(), clinicId);

        QueueTokenResponse result = toResponse(token, doctor.getFullName(), patientFullName(patient));
        broadcast(clinicId, result);
        return result;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Today's full queue for a clinic.
     * Pass doctorId to filter to a specific doctor's queue.
     * Returns all statuses so the receptionist sees the complete picture.
     */
    @Transactional(readOnly = true)
    public List<QueueTokenResponse> getTodayQueue(Long clinicId, Long doctorId) {
        List<QueueToken> tokens = doctorId != null
                ? queueRepository.findByClinicIdAndDoctorIdAndQueueDateOrderByTokenNumberAsc(
                        clinicId, doctorId, LocalDate.now())
                : queueRepository.findByClinicIdAndQueueDateOrderByDoctorIdAscTokenNumberAsc(
                        clinicId, LocalDate.now());
        return tokens.stream().map(this::toResponseWithNames).toList();
    }

    /** All WAITING tokens today — the "who is next" view. */
    @Transactional(readOnly = true)
    public List<QueueTokenResponse> getWaiting(Long clinicId, Long doctorId) {
        List<QueueToken> tokens = doctorId != null
                ? queueRepository.findByClinicIdAndDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
                        clinicId, doctorId, LocalDate.now(), QueueStatus.WAITING)
                : queueRepository.findByClinicIdAndQueueDateAndStatusOrderByDoctorIdAscTokenNumberAsc(
                        clinicId, LocalDate.now(), QueueStatus.WAITING);
        return tokens.stream().map(this::toResponseWithNames).toList();
    }

    /** All IN_PROGRESS tokens — who is currently with a doctor. */
    @Transactional(readOnly = true)
    public List<QueueTokenResponse> getCurrent(Long clinicId, Long doctorId) {
        List<QueueToken> tokens = doctorId != null
                ? queueRepository.findByClinicIdAndDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
                        clinicId, doctorId, LocalDate.now(), QueueStatus.IN_PROGRESS)
                : queueRepository.findByClinicIdAndQueueDateAndStatusOrderByDoctorIdAscTokenNumberAsc(
                        clinicId, LocalDate.now(), QueueStatus.IN_PROGRESS);
        return tokens.stream().map(this::toResponseWithNames).toList();
    }

    // ── Status transitions ────────────────────────────────────────────────────

    /**
     * WAITING → CALLED.
     * Announces "Token #N, please proceed to Dr. X's room."
     */
    @Transactional
    public QueueTokenResponse callToken(Long clinicId, Long tokenId, UserPrincipal principal) {
        QueueToken token = findOrThrow(clinicId, tokenId);
        assertStatus(token, QueueStatus.WAITING, "call");

        token.setStatus(QueueStatus.CALLED);
        token.setCalledAt(LocalDateTime.now());
        token.setCalledBy(principal.getId());

        log.info("Token #{} called: clinic={}, doctor={}", token.getTokenNumber(),
                clinicId, token.getDoctorId());
        QueueTokenResponse tokenResult = toResponseWithNames(queueRepository.save(token));

        String doctorName = doctorRepository.findByIdAndDeletedFalse(token.getDoctorId())
                .map(Doctor::getFullName).orElse("your doctor");
        notificationService.notifyTokenCalled(clinicId, token.getId(),
                token.getPatientId(), token.getTokenNumber(), doctorName);

        broadcast(clinicId, tokenResult);
        return tokenResult;
    }

    /**
     * CALLED → IN_PROGRESS.
     * Doctor has started the consultation.
     * Also marks the linked appointment as IN_PROGRESS (if any).
     */
    @Transactional
    public QueueTokenResponse startConsultation(Long clinicId, Long tokenId,
                                                 UserPrincipal principal) {
        QueueToken token = findOrThrow(clinicId, tokenId);
        assertStatus(token, QueueStatus.CALLED, "start");

        token.setStatus(QueueStatus.IN_PROGRESS);
        token.setStartedAt(LocalDateTime.now());

        // Mirror status on linked appointment
        if (token.getAppointmentId() != null) {
            appointmentRepository.findById(token.getAppointmentId()).ifPresent(appt -> {
                if (appt.getStatus() == AppointmentStatus.CONFIRMED) {
                    appt.setStatus(AppointmentStatus.IN_PROGRESS);
                    appointmentRepository.save(appt);
                }
            });
        }

        QueueTokenResponse result = toResponseWithNames(queueRepository.save(token));
        broadcast(clinicId, result);
        return result;
    }

    /**
     * IN_PROGRESS → COMPLETED.
     * Consultation finished. Also marks the linked appointment COMPLETED.
     */
    @Transactional
    public QueueTokenResponse completeToken(Long clinicId, Long tokenId,
                                             UserPrincipal principal) {
        QueueToken token = findOrThrow(clinicId, tokenId);
        assertStatus(token, QueueStatus.IN_PROGRESS, "complete");

        token.setStatus(QueueStatus.COMPLETED);
        token.setCompletedAt(LocalDateTime.now());

        if (token.getAppointmentId() != null) {
            appointmentRepository.findById(token.getAppointmentId()).ifPresent(appt -> {
                if (appt.getStatus() == AppointmentStatus.IN_PROGRESS
                        || appt.getStatus() == AppointmentStatus.CONFIRMED) {
                    appt.setStatus(AppointmentStatus.COMPLETED);
                    appointmentRepository.save(appt);
                }
            });
        }

        log.info("Token #{} completed: clinic={}, doctor={}", token.getTokenNumber(),
                clinicId, token.getDoctorId());
        QueueTokenResponse result = toResponseWithNames(queueRepository.save(token));
        broadcast(clinicId, result);
        return result;
    }

    /**
     * CALLED → SKIPPED.
     * Patient didn't respond when called. The slot is freed for the next patient.
     * The patient can be re-added to the end of the queue via /recall.
     */
    @Transactional
    public QueueTokenResponse skipToken(Long clinicId, Long tokenId, UserPrincipal principal) {
        QueueToken token = findOrThrow(clinicId, tokenId);
        assertStatus(token, QueueStatus.CALLED, "skip");

        token.setStatus(QueueStatus.SKIPPED);
        log.info("Token #{} skipped: clinic={}, doctor={}", token.getTokenNumber(),
                clinicId, token.getDoctorId());
        QueueTokenResponse result = toResponseWithNames(queueRepository.save(token));
        broadcast(clinicId, result);
        return result;
    }

    /**
     * SKIPPED → WAITING (re-issued with a new token number at the end of the queue).
     *
     * Why a new token number instead of restoring the old one?
     * The patient goes to the end of the queue — they had their chance and didn't respond.
     * A new token number makes the ordering fair for patients who were waiting behind them.
     */
    @Transactional
    public QueueTokenResponse recallToken(Long clinicId, Long tokenId, UserPrincipal principal) {
        QueueToken token = findOrThrow(clinicId, tokenId);
        assertStatus(token, QueueStatus.SKIPPED, "recall");

        int newTokenNumber = queueRepository.getMaxTokenNumber(
                clinicId, token.getDoctorId(), token.getQueueDate()) + 1;

        token.setStatus(QueueStatus.WAITING);
        token.setTokenNumber(newTokenNumber);
        token.setCalledAt(null);
        token.setCalledBy(null);

        log.info("Token recalled with new number #{}: clinic={}, doctor={}",
                newTokenNumber, clinicId, token.getDoctorId());
        QueueTokenResponse result = toResponseWithNames(queueRepository.save(token));
        broadcast(clinicId, result);
        return result;
    }

    /**
     * Any non-terminal state → CANCELLED.
     * Patient left the clinic without being seen.
     */
    @Transactional
    public QueueTokenResponse cancelToken(Long clinicId, Long tokenId, UserPrincipal principal) {
        QueueToken token = findOrThrow(clinicId, tokenId);

        if (TERMINAL.contains(token.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Token is already " + token.getStatus().name().toLowerCase());
        }

        token.setStatus(QueueStatus.CANCELLED);
        log.info("Token #{} cancelled: clinic={}, doctor={}", token.getTokenNumber(),
                clinicId, token.getDoctorId());
        QueueTokenResponse result = toResponseWithNames(queueRepository.save(token));
        broadcast(clinicId, result);
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private QueueToken findOrThrow(Long clinicId, Long tokenId) {
        return queueRepository.findByIdAndClinicId(tokenId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Queue token not found: " + tokenId));
    }

    private void assertStatus(QueueToken token, QueueStatus required, String action) {
        if (token.getStatus() != required) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Cannot " + action + " a token that is " + token.getStatus().name().toLowerCase()
                            + ". Token must be " + required.name().toLowerCase() + ".");
        }
    }

    private String patientFullName(Patient p) {
        return p.getLastName() != null
                ? p.getFirstName() + " " + p.getLastName()
                : p.getFirstName();
    }

    private QueueTokenResponse toResponseWithNames(QueueToken token) {
        String doctorName = doctorRepository.findByIdAndDeletedFalse(token.getDoctorId())
                .map(Doctor::getFullName).orElse(null);
        String patientName = patientRepository.findByIdAndDeletedFalse(token.getPatientId())
                .map(this::patientFullName).orElse(null);
        return toResponse(token, doctorName, patientName);
    }

    private QueueTokenResponse toResponse(QueueToken t, String doctorName, String patientName) {
        Integer tokensAhead = null;
        Integer estimatedWait = null;

        if (t.getStatus() == QueueStatus.WAITING) {
            int ahead = queueRepository.countTokensAhead(
                    t.getClinicId(), t.getDoctorId(), t.getQueueDate(), t.getTokenNumber());
            tokensAhead = ahead;
            estimatedWait = ahead * 10; // 10 minutes per patient (average slot size)
        }

        return QueueTokenResponse.builder()
                .id(t.getId())
                .clinicId(t.getClinicId())
                .doctorId(t.getDoctorId())
                .patientId(t.getPatientId())
                .appointmentId(t.getAppointmentId())
                .doctorName(doctorName)
                .patientName(patientName)
                .tokenNumber(t.getTokenNumber())
                .queueDate(t.getQueueDate())
                .status(t.getStatus())
                .notes(t.getNotes())
                .tokensAhead(tokensAhead)
                .estimatedWaitMins(estimatedWait)
                .calledAt(t.getCalledAt())
                .startedAt(t.getStartedAt())
                .completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
