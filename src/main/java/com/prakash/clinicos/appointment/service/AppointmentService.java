package com.prakash.clinicos.appointment.service;

import com.prakash.clinicos.appointment.dto.request.*;
import com.prakash.clinicos.appointment.dto.response.AppointmentResponse;
import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.dto.response.DoctorAvailabilityResponse;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.doctor.repository.DoctorTreatmentRepository;
import com.prakash.clinicos.doctor.service.DoctorAvailabilityService;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.doctor.entity.TreatmentType;
import com.prakash.clinicos.doctor.repository.TreatmentTypeRepository;
import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.notification.service.NotificationService;
import com.prakash.clinicos.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AppointmentService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * These statuses free the slot — they are excluded from double-booking checks
     * and from the booked-slot filter in the availability algorithm.
     */
    private static final Set<AppointmentStatus> TERMINAL_STATUSES = Set.of(
            AppointmentStatus.CANCELLED,
            AppointmentStatus.RESCHEDULED,
            AppointmentStatus.NO_SHOW
    );

    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;
    private final DoctorTreatmentRepository doctorTreatmentRepository;
    private final DoctorAvailabilityService availabilityService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               ClinicRepository clinicRepository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository,
                               TreatmentTypeRepository treatmentTypeRepository,
                               DoctorTreatmentRepository doctorTreatmentRepository,
                               DoctorAvailabilityService availabilityService,
                               NotificationService notificationService,
                               AuditService auditService) {
        this.appointmentRepository = appointmentRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.doctorTreatmentRepository = doctorTreatmentRepository;
        this.availabilityService = availabilityService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    // ── Book ──────────────────────────────────────────────────────────────────

    /**
     * Books an appointment after running a full 8-step validation:
     *
     *  1. Clinic exists
     *  2. Doctor belongs to this clinic and is active
     *  3. Patient belongs to this clinic
     *  4. Treatment type (if given) belongs to this clinic
     *  5. Date is not in the past
     *  6. Doctor is available on that date (runs the 11-step availability algorithm)
     *  7. startTime is a valid free slot (within working windows, not in break, not booked)
     *  8. All slots from startTime to endTime are free (duration-aware conflict check)
     */
    @Transactional
    public AppointmentResponse bookAppointment(Long clinicId, BookAppointmentRequest req,
                                               UserPrincipal principal) {
        // 1. Clinic
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found: " + clinicId));

        // 2. Doctor
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(req.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Doctor not found: " + req.getDoctorId()));
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Doctor does not belong to this clinic");
        }
        if (!doctor.isActive()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Doctor is not currently active");
        }

        // 3. Patient
        Patient patient = patientRepository.findByIdAndDeletedFalse(req.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Patient not found: " + req.getPatientId()));
        if (!patient.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Patient does not belong to this clinic");
        }

        // 4. Treatment type + duration resolution
        int durationMins = 10; // default: one 10-minute slot
        String treatmentName = null;
        if (req.getTreatmentTypeId() != null) {
            TreatmentType tt = treatmentTypeRepository
                    .findByIdAndClinicIdAndDeletedFalse(req.getTreatmentTypeId(), clinicId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                            "Treatment type not found: " + req.getTreatmentTypeId()));
            treatmentName = tt.getName();

            // Use doctor's custom duration if configured, otherwise the treatment default
            durationMins = doctorTreatmentRepository
                    .findByDoctorIdAndTreatmentTypeId(doctor.getId(), tt.getId())
                    .filter(dt -> dt.getCustomDurationMins() != null)
                    .map(dt -> dt.getCustomDurationMins())
                    .orElse(tt.getDefaultDurationMins());
        }

        LocalTime startTime = req.getStartTime();
        LocalTime endTime = startTime.plusMinutes(durationMins);

        // 5. Date must not be in the past (annotation catches it but double-check here)
        if (req.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot book appointments in the past");
        }

        // 6 + 7. Run availability algorithm — gives us all free 10-min slots for this doctor/date
        DoctorAvailabilityResponse avail =
                availabilityService.computeAvailability(doctor.getId(), req.getAppointmentDate());

        if (!avail.isAvailable()) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Doctor is not available on " + req.getAppointmentDate()
                            + ". Reason: " + avail.getUnavailableReason());
        }

        // 7. Validate that every 10-min chunk of the appointment duration is a free slot.
        //    This checks: on 10-min boundary, within working windows, not in any break,
        //    and no existing appointment occupies any of those slots.
        validateSlotsAreFree(avail, startTime, durationMins, req.getAppointmentDate());

        // 8. DB-level conflict check as a safety net (handles race conditions)
        if (appointmentRepository.existsConflictingAppointment(
                doctor.getId(), req.getAppointmentDate(), startTime, endTime)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Slot " + startTime.format(TIME_FMT) + " is already booked for this doctor");
        }

        Appointment appointment = Appointment.builder()
                .clinicId(clinicId)
                .doctorId(doctor.getId())
                .patientId(patient.getId())
                .treatmentTypeId(req.getTreatmentTypeId())
                .appointmentDate(req.getAppointmentDate())
                .startTime(startTime)
                .endTime(endTime)
                .durationMins(durationMins)
                .reason(req.getReason())
                .notes(req.getNotes())
                .bookedBy(principal.getId())
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment booked: id={}, doctor={}, patient={}, date={}, time={}",
                appointment.getId(), doctor.getId(), patient.getId(),
                req.getAppointmentDate(), startTime.format(TIME_FMT));

        notificationService.notifyAppointmentBooked(clinicId, appointment.getId(),
                patient.getId(), doctor.getFullName(), req.getAppointmentDate(), startTime);

        AppointmentResponse booked = toResponse(appointment, doctor.getFullName(), patientFullName(patient), treatmentName);
        auditService.log(clinicId, "APPOINTMENT", appointment.getId(), AuditAction.CREATE, null, booked, principal.getId());
        return booked;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long clinicId, Long appointmentId) {
        Appointment appt = findOrThrow(clinicId, appointmentId);
        return toResponseWithNames(appt);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> searchAppointments(Long clinicId, Long doctorId,
                                                         Long patientId, LocalDate date,
                                                         AppointmentStatus status,
                                                         Pageable pageable) {
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found: " + clinicId));

        return appointmentRepository
                .searchAppointments(clinicId, doctorId, patientId, date, status, pageable)
                .map(this::toResponseWithNames);
    }

    /** Doctor's full schedule for a specific date (all statuses). */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorDaySchedule(Long clinicId, Long doctorId,
                                                           LocalDate date) {
        return appointmentRepository
                .findByDoctorIdAndAppointmentDateOrderByStartTime(doctorId, date)
                .stream().map(this::toResponseWithNames).toList();
    }

    /** Patient's full appointment history (all clinics, most recent first). */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientHistory(Long clinicId, Long patientId) {
        patientRepository.findByIdAndDeletedFalse(patientId)
                .filter(p -> p.getClinicId().equals(clinicId))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Patient not found in this clinic"));

        return appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId)
                .stream().map(this::toResponseWithNames).toList();
    }

    // ── Status transitions ────────────────────────────────────────────────────

    /**
     * Advances an appointment through its status lifecycle.
     *
     * Valid transitions enforced here:
     *   PENDING    → CONFIRMED
     *   CONFIRMED  → IN_PROGRESS
     *   CONFIRMED  → COMPLETED  (skip IN_PROGRESS if needed)
     *   IN_PROGRESS → COMPLETED
     *   CONFIRMED  → NO_SHOW
     *
     * CANCELLED uses its own endpoint. RESCHEDULED is set by the reschedule endpoint.
     */
    @Transactional
    public AppointmentResponse updateStatus(Long clinicId, Long appointmentId,
                                             UpdateAppointmentStatusRequest req,
                                             UserPrincipal principal) {
        Appointment appt = findOrThrow(clinicId, appointmentId);
        assertNotTerminal(appt);
        validateTransition(appt.getStatus(), req.getStatus());

        AppointmentResponse beforeStatus = toResponseWithNames(appt);

        appt.setStatus(req.getStatus());
        if (req.getNotes() != null) appt.setNotes(req.getNotes());

        // Track confirmation timestamp
        if (req.getStatus() == AppointmentStatus.CONFIRMED) {
            appt.setConfirmedBy(principal.getId());
            appt.setConfirmedAt(LocalDateTime.now());
        }

        AppointmentResponse result = toResponseWithNames(appointmentRepository.save(appt));

        if (req.getStatus() == AppointmentStatus.CONFIRMED) {
            notificationService.notifyAppointmentConfirmed(clinicId, appt.getId(),
                    appt.getPatientId(), result.getDoctorName(),
                    appt.getAppointmentDate(), appt.getStartTime());
        }

        auditService.log(clinicId, "APPOINTMENT", appointmentId, AuditAction.UPDATE, beforeStatus, result, principal.getId());
        return result;
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long clinicId, Long appointmentId,
                                                  CancelAppointmentRequest req,
                                                  UserPrincipal principal) {
        Appointment appt = findOrThrow(clinicId, appointmentId);
        assertNotTerminal(appt);

        AppointmentResponse beforeCancel = toResponseWithNames(appt);

        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancelledBy(principal.getId());
        appt.setCancelledAt(LocalDateTime.now());
        appt.setCancellationReason(req.getReason());

        log.info("Appointment cancelled: id={}, by={}", appointmentId, principal.getEmail());
        AppointmentResponse cancelResult = toResponseWithNames(appointmentRepository.save(appt));

        notificationService.notifyAppointmentCancelled(clinicId, appt.getId(),
                appt.getPatientId(), cancelResult.getDoctorName(), appt.getAppointmentDate());

        auditService.log(clinicId, "APPOINTMENT", appointmentId, AuditAction.UPDATE, beforeCancel, cancelResult, principal.getId());
        return cancelResult;
    }

    /**
     * Reschedules an appointment:
     *   1. Marks the old appointment as RESCHEDULED.
     *   2. Creates a new PENDING appointment with the new date/time.
     *   3. The new appointment stores rescheduledFromId = old appointment id.
     *
     * Why two records instead of updating in place?
     * This preserves the audit trail — you can see the full history of when a patient
     * changed their appointment and why.
     */
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long clinicId, Long appointmentId,
                                                      RescheduleAppointmentRequest req,
                                                      UserPrincipal principal) {
        Appointment old = findOrThrow(clinicId, appointmentId);
        assertNotTerminal(old);

        // Validate the new slot using the same booking validation logic
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(old.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));

        LocalTime newEndTime = req.getNewStartTime().plusMinutes(old.getDurationMins());

        DoctorAvailabilityResponse avail =
                availabilityService.computeAvailability(doctor.getId(), req.getNewDate());

        if (!avail.isAvailable()) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Doctor is not available on " + req.getNewDate()
                            + ". Reason: " + avail.getUnavailableReason());
        }

        validateSlotsAreFree(avail, req.getNewStartTime(), old.getDurationMins(), req.getNewDate());

        if (appointmentRepository.existsConflictingAppointment(
                doctor.getId(), req.getNewDate(), req.getNewStartTime(), newEndTime)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Slot " + req.getNewStartTime().format(TIME_FMT)
                            + " on " + req.getNewDate() + " is already booked");
        }

        // Mark old appointment as RESCHEDULED
        AppointmentResponse beforeReschedule = toResponseWithNames(old);
        old.setStatus(AppointmentStatus.RESCHEDULED);
        old.setCancellationReason(req.getReason()); // reuse this field for reschedule reason
        AppointmentResponse oldRescheduled = toResponseWithNames(appointmentRepository.save(old));
        auditService.log(clinicId, "APPOINTMENT", appointmentId, AuditAction.UPDATE, beforeReschedule, oldRescheduled, principal.getId());

        // Create new appointment
        Appointment newAppt = Appointment.builder()
                .clinicId(old.getClinicId())
                .doctorId(old.getDoctorId())
                .patientId(old.getPatientId())
                .treatmentTypeId(old.getTreatmentTypeId())
                .appointmentDate(req.getNewDate())
                .startTime(req.getNewStartTime())
                .endTime(newEndTime)
                .durationMins(old.getDurationMins())
                .reason(old.getReason())
                .notes(old.getNotes())
                .rescheduledFromId(old.getId())
                .bookedBy(principal.getId())
                .build();

        newAppt = appointmentRepository.save(newAppt);
        log.info("Appointment rescheduled: old={} → new={}, date={}, time={}",
                old.getId(), newAppt.getId(), req.getNewDate(), req.getNewStartTime().format(TIME_FMT));

        AppointmentResponse rescheduleResult = toResponseWithNames(newAppt);
        notificationService.notifyAppointmentRescheduled(clinicId, newAppt.getId(),
                newAppt.getPatientId(), rescheduleResult.getDoctorName(),
                req.getNewDate(), req.getNewStartTime());

        auditService.log(clinicId, "APPOINTMENT", newAppt.getId(), AuditAction.CREATE, null, rescheduleResult, principal.getId());
        return rescheduleResult;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Appointment findOrThrow(Long clinicId, Long appointmentId) {
        return appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Appointment not found: " + appointmentId));
    }

    private void assertNotTerminal(Appointment appt) {
        if (TERMINAL_STATUSES.contains(appt.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Appointment is already " + appt.getStatus().name().toLowerCase()
                            + " and cannot be modified");
        }
        if (appt.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Appointment is already completed and cannot be modified");
        }
    }

    /**
     * Validates that every 10-minute chunk of the requested appointment duration
     * is a free slot in the availability response.
     *
     * Why this instead of a single slot check?
     * For a 30-min appointment at 09:00, we need 09:00, 09:10, and 09:20 all free.
     * The availability algorithm already deducts existing bookings, so this check
     * also catches double-booking for multi-slot treatments.
     */
    private void validateSlotsAreFree(DoctorAvailabilityResponse avail,
                                       LocalTime startTime, int durationMins,
                                       LocalDate date) {
        // Check start time is on 10-minute boundary
        if (startTime.getMinute() % 10 != 0 || startTime.getSecond() != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "startTime must be on a 10-minute boundary (e.g. 09:00, 09:30). Got: "
                            + startTime.format(TIME_FMT));
        }

        List<String> freeSlots = avail.getSlots();
        LocalTime cursor = startTime;
        LocalTime endTime = startTime.plusMinutes(durationMins);

        while (cursor.isBefore(endTime)) {
            String slotStr = cursor.format(TIME_FMT);
            if (!freeSlots.contains(slotStr)) {
                throw new AppException(HttpStatus.CONFLICT,
                        "Slot " + slotStr + " on " + date + " is not available for this doctor. "
                                + "It may be outside working hours, in a break, or already booked.");
            }
            cursor = cursor.plusMinutes(10);
        }
    }

    private void validateTransition(AppointmentStatus from, AppointmentStatus to) {
        boolean valid = switch (from) {
            case PENDING     -> to == AppointmentStatus.CONFIRMED;
            case CONFIRMED   -> to == AppointmentStatus.IN_PROGRESS
                                || to == AppointmentStatus.COMPLETED
                                || to == AppointmentStatus.NO_SHOW;
            case IN_PROGRESS -> to == AppointmentStatus.COMPLETED;
            default          -> false;
        };

        if (!valid) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Cannot transition appointment from " + from + " to " + to
                            + ". Use the cancel or reschedule endpoints for those operations.");
        }
    }

    private String patientFullName(Patient p) {
        return p.getLastName() != null
                ? p.getFirstName() + " " + p.getLastName()
                : p.getFirstName();
    }

    private AppointmentResponse toResponseWithNames(Appointment appt) {
        String doctorName = doctorRepository.findByIdAndDeletedFalse(appt.getDoctorId())
                .map(Doctor::getFullName).orElse(null);
        String patientName = patientRepository.findByIdAndDeletedFalse(appt.getPatientId())
                .map(this::patientFullName).orElse(null);
        String treatmentName = appt.getTreatmentTypeId() != null
                ? treatmentTypeRepository.findById(appt.getTreatmentTypeId())
                        .map(TreatmentType::getName).orElse(null)
                : null;

        return toResponse(appt, doctorName, patientName, treatmentName);
    }

    private AppointmentResponse toResponse(Appointment a, String doctorName,
                                            String patientName, String treatmentName) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .clinicId(a.getClinicId())
                .doctorId(a.getDoctorId())
                .patientId(a.getPatientId())
                .treatmentTypeId(a.getTreatmentTypeId())
                .doctorName(doctorName)
                .patientName(patientName)
                .treatmentName(treatmentName)
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .durationMins(a.getDurationMins())
                .status(a.getStatus())
                .reason(a.getReason())
                .notes(a.getNotes())
                .cancellationReason(a.getCancellationReason())
                .cancelledAt(a.getCancelledAt())
                .rescheduledFromId(a.getRescheduledFromId())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
