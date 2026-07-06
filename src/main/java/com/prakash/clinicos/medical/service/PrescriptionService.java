package com.prakash.clinicos.medical.service;

import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.medical.dto.request.CreatePrescriptionRequest;
import com.prakash.clinicos.medical.dto.request.PrescriptionMedicineRequest;
import com.prakash.clinicos.medical.dto.request.UpdatePrescriptionRequest;
import com.prakash.clinicos.medical.dto.response.PrescriptionMedicineResponse;
import com.prakash.clinicos.medical.dto.response.PrescriptionResponse;
import com.prakash.clinicos.medical.entity.Prescription;
import com.prakash.clinicos.medical.entity.PrescriptionMedicine;
import com.prakash.clinicos.medical.repository.PrescriptionMedicineRepository;
import com.prakash.clinicos.medical.repository.PrescriptionRepository;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMedicineRepository medicineRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PrescriptionMedicineRepository medicineRepository,
                               ClinicRepository clinicRepository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository,
                               AppointmentRepository appointmentRepository,
                               AuditService auditService) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditService = auditService;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public PrescriptionResponse create(Long clinicId,
                                       CreatePrescriptionRequest req,
                                       UserPrincipal principal) {
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Doctor does not belong to this clinic");
        }

        Patient patient = patientRepository.findByIdAndDeletedFalse(req.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (!patient.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Patient does not belong to this clinic");
        }

        if (req.getAppointmentId() != null) {
            appointmentRepository.findByIdAndClinicId(req.getAppointmentId(), clinicId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found"));
            if (prescriptionRepository.existsByAppointmentId(req.getAppointmentId())) {
                throw new AppException(HttpStatus.CONFLICT,
                        "A prescription already exists for this appointment. Use PUT to update it.");
            }
        }

        Prescription prescription = prescriptionRepository.save(Prescription.builder()
                .clinicId(clinicId)
                .doctorId(req.getDoctorId())
                .patientId(req.getPatientId())
                .appointmentId(req.getAppointmentId())
                .diagnosis(req.getDiagnosis())
                .instructions(req.getInstructions())
                .followUpDate(req.getFollowUpDate())
                .createdBy(principal.getId())
                .updatedBy(principal.getId())
                .build());

        List<PrescriptionMedicine> medicines = saveMedicines(prescription.getId(), req.getMedicines());
        PrescriptionResponse created = toResponse(prescription, medicines, doctor, patient);
        auditService.log(clinicId, "PRESCRIPTION", prescription.getId(), AuditAction.CREATE, null, created, principal.getId());
        return created;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public PrescriptionResponse update(Long clinicId,
                                       Long prescriptionId,
                                       UpdatePrescriptionRequest req,
                                       UserPrincipal principal) {
        Prescription prescription = load(clinicId, prescriptionId);

        PrescriptionResponse before = toFullResponse(prescription);

        if (req.getDiagnosis() != null)    prescription.setDiagnosis(req.getDiagnosis());
        if (req.getInstructions() != null) prescription.setInstructions(req.getInstructions());
        if (req.getFollowUpDate() != null) prescription.setFollowUpDate(req.getFollowUpDate());
        prescription.setUpdatedBy(principal.getId());

        prescriptionRepository.save(prescription);

        List<PrescriptionMedicine> medicines;
        if (req.getMedicines() != null) {
            // Replace all medicines when list is provided
            medicineRepository.deleteByPrescriptionId(prescriptionId);
            medicines = saveMedicines(prescriptionId, req.getMedicines());
        } else {
            medicines = medicineRepository.findByPrescriptionIdOrderById(prescriptionId);
        }

        Doctor doctor = doctorRepository.findById(prescription.getDoctorId()).orElse(null);
        Patient patient = patientRepository.findByIdAndDeletedFalse(prescription.getPatientId()).orElse(null);
        PrescriptionResponse after = toResponse(prescription, medicines, doctor, patient);
        auditService.log(clinicId, "PRESCRIPTION", prescriptionId, AuditAction.UPDATE, before, after, principal.getId());
        return after;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PrescriptionResponse getById(Long clinicId, Long prescriptionId) {
        Prescription prescription = load(clinicId, prescriptionId);
        return toFullResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getByAppointment(Long clinicId, Long appointmentId) {
        Prescription prescription = prescriptionRepository
                .findByAppointmentIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No prescription found for this appointment"));
        return toFullResponse(prescription);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getByPatient(Long clinicId, Long patientId, Pageable pageable) {
        patientRepository.findByIdAndDeletedFalse(patientId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));
        return prescriptionRepository
                .findByPatientIdAndClinicIdOrderByCreatedAtDesc(patientId, clinicId, pageable)
                .map(this::toFullResponse);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Prescription load(Long clinicId, Long id) {
        return prescriptionRepository.findByIdAndClinicId(id, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Prescription not found for this clinic"));
    }

    private PrescriptionResponse toFullResponse(Prescription prescription) {
        List<PrescriptionMedicine> medicines =
                medicineRepository.findByPrescriptionIdOrderById(prescription.getId());
        Doctor doctor = doctorRepository.findById(prescription.getDoctorId()).orElse(null);
        Patient patient = patientRepository.findByIdAndDeletedFalse(prescription.getPatientId()).orElse(null);
        return toResponse(prescription, medicines, doctor, patient);
    }

    private PrescriptionResponse toResponse(Prescription p,
                                             List<PrescriptionMedicine> medicines,
                                             Doctor doctor,
                                             Patient patient) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .clinicId(p.getClinicId())
                .doctorId(p.getDoctorId())
                .patientId(p.getPatientId())
                .appointmentId(p.getAppointmentId())
                .doctorName(doctor != null ? "Dr. " + doctor.getFullName() : null)
                .patientName(patient != null
                        ? patient.getFirstName() + " " + patient.getLastName() : null)
                .diagnosis(p.getDiagnosis())
                .instructions(p.getInstructions())
                .followUpDate(p.getFollowUpDate())
                .medicines(medicines.stream().map(this::toMedicineResponse).toList())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PrescriptionMedicineResponse toMedicineResponse(PrescriptionMedicine m) {
        return PrescriptionMedicineResponse.builder()
                .id(m.getId())
                .medicineName(m.getMedicineName())
                .dosage(m.getDosage())
                .frequency(m.getFrequency())
                .durationDays(m.getDurationDays())
                .route(m.getRoute())
                .notes(m.getNotes())
                .build();
    }

    private List<PrescriptionMedicine> saveMedicines(Long prescriptionId,
                                                      List<PrescriptionMedicineRequest> requests) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        return requests.stream()
                .map(r -> medicineRepository.save(PrescriptionMedicine.builder()
                        .prescriptionId(prescriptionId)
                        .medicineName(r.getMedicineName())
                        .dosage(r.getDosage())
                        .frequency(r.getFrequency())
                        .durationDays(r.getDurationDays())
                        .route(r.getRoute())
                        .notes(r.getNotes())
                        .build()))
                .toList();
    }
}
