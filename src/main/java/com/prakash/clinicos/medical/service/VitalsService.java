package com.prakash.clinicos.medical.service;

import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.medical.dto.request.RecordVitalsRequest;
import com.prakash.clinicos.medical.dto.response.VitalsResponse;
import com.prakash.clinicos.medical.entity.Vitals;
import com.prakash.clinicos.medical.repository.VitalsRepository;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VitalsService {

    private final VitalsRepository vitalsRepository;
    private final ClinicRepository clinicRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public VitalsService(VitalsRepository vitalsRepository,
                         ClinicRepository clinicRepository,
                         PatientRepository patientRepository,
                         AppointmentRepository appointmentRepository) {
        this.vitalsRepository = vitalsRepository;
        this.clinicRepository = clinicRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // ── Record (upsert) ───────────────────────────────────────────────────────

    /**
     * Creates or updates vitals for the given appointment.
     * One vitals record per appointment — subsequent calls overwrite the previous values.
     */
    @Transactional
    public VitalsResponse record(Long clinicId,
                                  Long appointmentId,
                                  RecordVitalsRequest req,
                                  UserPrincipal principal) {
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));

        var appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (allNull(req)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "At least one vital sign must be provided");
        }

        Vitals vitals = vitalsRepository
                .findByAppointmentIdAndClinicId(appointmentId, clinicId)
                .orElseGet(() -> Vitals.builder()
                        .clinicId(clinicId)
                        .patientId(appointment.getPatientId())
                        .appointmentId(appointmentId)
                        .build());

        vitals.setSystolicBp(req.getSystolicBp());
        vitals.setDiastolicBp(req.getDiastolicBp());
        vitals.setPulseBpm(req.getPulseBpm());
        vitals.setTemperatureCelsius(req.getTemperatureCelsius());
        vitals.setWeightKg(req.getWeightKg());
        vitals.setHeightCm(req.getHeightCm());
        vitals.setSpo2Percent(req.getSpo2Percent());
        vitals.setNotes(req.getNotes());
        vitals.setRecordedBy(principal.getId());
        vitals.setRecordedAt(LocalDateTime.now());

        vitals = vitalsRepository.save(vitals);
        Patient patient = patientRepository.findByIdAndDeletedFalse(vitals.getPatientId()).orElse(null);
        return toResponse(vitals, patient);
    }

    // ── Read single ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public VitalsResponse getByAppointment(Long clinicId, Long appointmentId) {
        Vitals vitals = vitalsRepository.findByAppointmentIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No vitals recorded for this appointment"));
        Patient patient = patientRepository.findByIdAndDeletedFalse(vitals.getPatientId()).orElse(null);
        return toResponse(vitals, patient);
    }

    // ── History ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<VitalsResponse> getByPatient(Long clinicId, Long patientId, Pageable pageable) {
        patientRepository.findByIdAndDeletedFalse(patientId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));
        return vitalsRepository
                .findByPatientIdAndClinicIdOrderByRecordedAtDesc(patientId, clinicId, pageable)
                .map(v -> toResponse(v, null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean allNull(RecordVitalsRequest req) {
        return req.getSystolicBp() == null
                && req.getDiastolicBp() == null
                && req.getPulseBpm() == null
                && req.getTemperatureCelsius() == null
                && req.getWeightKg() == null
                && req.getHeightCm() == null
                && req.getSpo2Percent() == null;
    }

    private VitalsResponse toResponse(Vitals v, Patient patient) {
        return VitalsResponse.builder()
                .id(v.getId())
                .clinicId(v.getClinicId())
                .patientId(v.getPatientId())
                .appointmentId(v.getAppointmentId())
                .patientName(patient != null
                        ? patient.getFirstName() + " " + patient.getLastName() : null)
                .systolicBp(v.getSystolicBp())
                .diastolicBp(v.getDiastolicBp())
                .pulseBpm(v.getPulseBpm())
                .temperatureCelsius(v.getTemperatureCelsius())
                .weightKg(v.getWeightKg())
                .heightCm(v.getHeightCm())
                .spo2Percent(v.getSpo2Percent())
                .notes(v.getNotes())
                .recordedAt(v.getRecordedAt())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
