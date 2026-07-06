package com.prakash.clinicos.patient.service;

import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.patient.dto.request.CreatePatientRequest;
import com.prakash.clinicos.patient.dto.request.UpdatePatientRequest;
import com.prakash.clinicos.patient.dto.response.PatientResponse;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import com.prakash.clinicos.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final SubscriptionService subscriptionService;
    private final AuditService auditService;

    public PatientService(PatientRepository patientRepository,
                          ClinicRepository clinicRepository,
                          SubscriptionService subscriptionService,
                          AuditService auditService) {
        this.patientRepository = patientRepository;
        this.clinicRepository = clinicRepository;
        this.subscriptionService = subscriptionService;
        this.auditService = auditService;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public PatientResponse createPatient(Long clinicId, CreatePatientRequest req,
                                         UserPrincipal principal) {
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found with id: " + clinicId));
        assertClinicOwnership(clinicId, principal);

        // Enforce subscription plan monthly patient limit
        subscriptionService.enforcePatientMonthlyLimit(clinicId);

        // Phone must be unique per clinic — prevents duplicate registrations
        if (patientRepository.existsByClinicIdAndPhoneAndDeletedFalse(clinicId, req.getPhone())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A patient with phone '" + req.getPhone() + "' is already registered in this clinic");
        }

        // Validate userId if provided
        if (req.getUserId() != null
                && patientRepository.existsByUserIdAndDeletedFalse(req.getUserId())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "User " + req.getUserId() + " is already linked to another patient profile");
        }

        Patient patient = Patient.builder()
                .clinicId(clinicId)
                .userId(req.getUserId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .bloodGroup(req.getBloodGroup())
                .allergies(req.getAllergies())
                .chronicConditions(req.getChronicConditions())
                .currentMedications(req.getCurrentMedications())
                .emergencyContactName(req.getEmergencyContactName())
                .emergencyContactPhone(req.getEmergencyContactPhone())
                .emergencyContactRelation(req.getEmergencyContactRelation())
                .address(req.getAddress())
                .notes(req.getNotes())
                .createdBy(principal.getId())
                .build();

        patient = patientRepository.save(patient);
        log.info("Patient created: id={}, phone={}, clinic={}", patient.getId(), req.getPhone(), clinicId);
        PatientResponse created = toFullResponse(patient);
        auditService.log(clinicId, "PATIENT", patient.getId(), AuditAction.CREATE, null, created, principal.getId());
        return created;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long clinicId, Long patientId) {
        Patient patient = findPatientOrThrow(patientId);
        assertSameClinic(patient, clinicId);
        return toFullResponse(patient);
    }

    /**
     * GET /api/v1/patients/me — a logged-in patient views their own profile.
     * Finds the patient record linked to the caller's user account.
     */
    @Transactional(readOnly = true)
    public PatientResponse getMyProfile(UserPrincipal principal) {
        return patientRepository.findByUserIdAndDeletedFalse(principal.getId())
                .map(this::toFullResponse)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No patient profile linked to your account"));
    }

    /**
     * Search patients with optional full-text filter.
     *
     * q (search term) matches against: first+last name, phone, email.
     * activeOnly=true (default) excludes inactive patients from results.
     */
    @Transactional(readOnly = true)
    public Page<PatientResponse> searchPatients(Long clinicId, String q,
                                                boolean activeOnly, Pageable pageable) {
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found with id: " + clinicId));

        return patientRepository.searchPatients(clinicId, q, activeOnly, pageable)
                .map(this::toSummaryResponse);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public PatientResponse updatePatient(Long clinicId, Long patientId,
                                         UpdatePatientRequest req, UserPrincipal principal) {
        Patient patient = findPatientOrThrow(patientId);
        assertSameClinic(patient, clinicId);
        assertClinicOwnership(clinicId, principal);

        // Phone uniqueness check if phone is being changed
        if (req.getPhone() != null && !req.getPhone().equals(patient.getPhone())
                && patientRepository.existsByClinicIdAndPhoneAndDeletedFalse(clinicId, req.getPhone())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A patient with phone '" + req.getPhone() + "' is already registered in this clinic");
        }

        PatientResponse before = toFullResponse(patient);

        if (req.getFirstName() != null)               patient.setFirstName(req.getFirstName());
        if (req.getLastName() != null)                patient.setLastName(req.getLastName());
        if (req.getPhone() != null)                   patient.setPhone(req.getPhone());
        if (req.getEmail() != null)                   patient.setEmail(req.getEmail());
        if (req.getDateOfBirth() != null)             patient.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null)                  patient.setGender(req.getGender());
        if (req.getBloodGroup() != null)              patient.setBloodGroup(req.getBloodGroup());
        if (req.getAllergies() != null)               patient.setAllergies(req.getAllergies());
        if (req.getChronicConditions() != null)       patient.setChronicConditions(req.getChronicConditions());
        if (req.getCurrentMedications() != null)      patient.setCurrentMedications(req.getCurrentMedications());
        if (req.getEmergencyContactName() != null)    patient.setEmergencyContactName(req.getEmergencyContactName());
        if (req.getEmergencyContactPhone() != null)   patient.setEmergencyContactPhone(req.getEmergencyContactPhone());
        if (req.getEmergencyContactRelation() != null) patient.setEmergencyContactRelation(req.getEmergencyContactRelation());
        if (req.getAddress() != null)                 patient.setAddress(req.getAddress());
        if (req.getNotes() != null)                   patient.setNotes(req.getNotes());
        if (req.getActive() != null)                  patient.setActive(req.getActive());

        PatientResponse after = toFullResponse(patientRepository.save(patient));
        auditService.log(clinicId, "PATIENT", patientId, AuditAction.UPDATE, before, after, principal.getId());
        return after;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deletePatient(Long clinicId, Long patientId, UserPrincipal principal) {
        Patient patient = findPatientOrThrow(patientId);
        assertSameClinic(patient, clinicId);
        assertClinicOwnership(clinicId, principal);

        if (patient.isDeleted()) {
            throw new AppException(HttpStatus.CONFLICT, "Patient is already deleted");
        }

        PatientResponse beforeDelete = toFullResponse(patient);
        patient.setDeleted(true);
        patient.setDeletedAt(LocalDateTime.now());
        patient.setDeletedBy(principal.getId());
        patientRepository.save(patient);
        log.info("Patient soft-deleted: id={}, by={}", patientId, principal.getEmail());
        auditService.log(clinicId, "PATIENT", patientId, AuditAction.DELETE, beforeDelete, null, principal.getId());
    }

    // ── User account linking ──────────────────────────────────────────────────

    /**
     * Links a user account to this patient profile (for self-service portal access).
     * One user account can only be linked to one patient profile (enforced by UNIQUE constraint).
     */
    @Transactional
    public PatientResponse linkUserAccount(Long clinicId, Long patientId,
                                            Long userId, UserPrincipal principal) {
        Patient patient = findPatientOrThrow(patientId);
        assertSameClinic(patient, clinicId);
        assertClinicOwnership(clinicId, principal);

        if (patientRepository.existsByUserIdAndDeletedFalse(userId)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "User " + userId + " is already linked to another patient profile");
        }

        patient.setUserId(userId);
        return toFullResponse(patientRepository.save(patient));
    }

    /**
     * Unlinks the user account. The user account itself is not affected.
     * The patient's clinical record remains intact.
     */
    @Transactional
    public PatientResponse unlinkUserAccount(Long clinicId, Long patientId,
                                              UserPrincipal principal) {
        Patient patient = findPatientOrThrow(patientId);
        assertSameClinic(patient, clinicId);
        assertClinicOwnership(clinicId, principal);
        patient.setUserId(null);
        return toFullResponse(patientRepository.save(patient));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Patient findPatientOrThrow(Long patientId) {
        return patientRepository.findByIdAndDeletedFalse(patientId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Patient not found with id: " + patientId));
    }

    private void assertSameClinic(Patient patient, Long clinicId) {
        if (!patient.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Patient not found in this clinic");
        }
    }

    /**
     * CLINIC_ADMIN can manage patients only in their own clinic.
     * SUPER_ADMIN can manage any clinic.
     */
    private void assertClinicOwnership(Long clinicId, UserPrincipal principal) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) return;

        clinicRepository.findByIdAndDeletedFalse(clinicId).ifPresent(clinic -> {
            if (!clinic.getOwnerUserId().equals(principal.getId())) {
                throw new AppException(HttpStatus.FORBIDDEN,
                        "You do not have permission to manage this clinic's patients");
            }
        });
    }

    // ── Response mappers ──────────────────────────────────────────────────────

    /**
     * Full response — includes notes and all medical fields.
     * Used for single-patient GET.
     */
    private PatientResponse toFullResponse(Patient p) {
        return toResponseBuilder(p)
                .notes(p.getNotes())
                .build();
    }

    /**
     * Summary response — omits notes (internal staff field).
     * Used in list/search results to keep the payload lean.
     */
    private PatientResponse toSummaryResponse(Patient p) {
        return toResponseBuilder(p).build();
    }

    private PatientResponse.PatientResponseBuilder toResponseBuilder(Patient p) {
        String fullName = p.getLastName() != null
                ? p.getFirstName() + " " + p.getLastName()
                : p.getFirstName();

        return PatientResponse.builder()
                .id(p.getId())
                .clinicId(p.getClinicId())
                .userId(p.getUserId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .fullName(fullName)
                .phone(p.getPhone())
                .email(p.getEmail())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .bloodGroup(p.getBloodGroup())
                .allergies(p.getAllergies())
                .chronicConditions(p.getChronicConditions())
                .currentMedications(p.getCurrentMedications())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .emergencyContactRelation(p.getEmergencyContactRelation())
                .address(p.getAddress())
                .active(p.isActive())
                .deleted(p.isDeleted())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt());
    }
}
