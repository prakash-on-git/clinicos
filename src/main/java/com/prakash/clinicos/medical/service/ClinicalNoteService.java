package com.prakash.clinicos.medical.service;

import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.medical.dto.request.SaveClinicalNoteRequest;
import com.prakash.clinicos.medical.dto.response.ClinicalNoteResponse;
import com.prakash.clinicos.medical.entity.ClinicalNote;
import com.prakash.clinicos.medical.repository.ClinicalNoteRepository;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicalNoteService {

    private final ClinicalNoteRepository noteRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public ClinicalNoteService(ClinicalNoteRepository noteRepository,
                               ClinicRepository clinicRepository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository,
                               AppointmentRepository appointmentRepository) {
        this.noteRepository = noteRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // ── Save (upsert) ─────────────────────────────────────────────────────────

    /**
     * Creates or updates the SOAP note for an appointment.
     * A null field in the request leaves the existing value unchanged.
     */
    @Transactional
    public ClinicalNoteResponse save(Long clinicId,
                                      Long appointmentId,
                                      SaveClinicalNoteRequest req,
                                      UserPrincipal principal) {
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));

        var appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found"));

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));

        ClinicalNote note = noteRepository
                .findByAppointmentIdAndClinicId(appointmentId, clinicId)
                .orElseGet(() -> ClinicalNote.builder()
                        .clinicId(clinicId)
                        .doctorId(appointment.getDoctorId())
                        .patientId(appointment.getPatientId())
                        .appointmentId(appointmentId)
                        .createdBy(principal.getId())
                        .build());

        // Merge — null means "don't touch"; empty string means "clear"
        if (req.getSubjective() != null)  note.setSubjective(req.getSubjective());
        if (req.getObjective() != null)   note.setObjective(req.getObjective());
        if (req.getAssessment() != null)  note.setAssessment(req.getAssessment());
        if (req.getPlan() != null)        note.setPlan(req.getPlan());
        note.setUpdatedBy(principal.getId());

        note = noteRepository.save(note);

        Patient patient = patientRepository.findByIdAndDeletedFalse(note.getPatientId()).orElse(null);
        return toResponse(note, doctor, patient);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ClinicalNoteResponse getByAppointment(Long clinicId, Long appointmentId) {
        ClinicalNote note = noteRepository
                .findByAppointmentIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No clinical note found for this appointment"));
        Doctor doctor = doctorRepository.findById(note.getDoctorId()).orElse(null);
        Patient patient = patientRepository.findByIdAndDeletedFalse(note.getPatientId()).orElse(null);
        return toResponse(note, doctor, patient);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ClinicalNoteResponse toResponse(ClinicalNote n, Doctor doctor, Patient patient) {
        return ClinicalNoteResponse.builder()
                .id(n.getId())
                .clinicId(n.getClinicId())
                .doctorId(n.getDoctorId())
                .patientId(n.getPatientId())
                .appointmentId(n.getAppointmentId())
                .doctorName(doctor != null ? "Dr. " + doctor.getFullName() : null)
                .patientName(patient != null
                        ? patient.getFirstName() + " " + patient.getLastName() : null)
                .subjective(n.getSubjective())
                .objective(n.getObjective())
                .assessment(n.getAssessment())
                .plan(n.getPlan())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
