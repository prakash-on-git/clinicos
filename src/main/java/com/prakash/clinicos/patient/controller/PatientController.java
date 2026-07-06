package com.prakash.clinicos.patient.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.patient.dto.request.CreatePatientRequest;
import com.prakash.clinicos.patient.dto.request.UpdatePatientRequest;
import com.prakash.clinicos.patient.dto.response.PatientResponse;
import com.prakash.clinicos.patient.service.PatientService;
import com.prakash.clinicos.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Patient module.
 *
 * URL structure:
 *   /api/v1/clinics/{clinicId}/patients   – clinic-scoped patient CRUD + search
 *   /api/v1/patients/me                   – patient self-view (their own profile)
 *
 * Security:
 *   Patient data is private — all endpoints require authentication.
 *   Write operations (create/update/delete/link) require CLINIC_ADMIN or SUPER_ADMIN.
 *   Read operations require any valid JWT (receptionist, doctor, or the patient themselves).
 *   The GET /api/v1/clinics/** wildcard in SecurityConfig is overridden for patient paths
 *   by the more-specific rule added before the wildcard.
 */
@Tag(name = "Patients", description = "Register, search, and manage patient profiles including medical history.")
@RestController
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Patient CRUD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/clinics/{clinicId}/patients
     * Register a new patient under this clinic.
     * Phone number is unique per clinic — prevents duplicate registrations.
     */
    @PostMapping("/api/v1/clinics/{clinicId}/patients")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> createPatient(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.createPatient(clinicId, request, principal));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/patients?q=&activeOnly=true&page=0&size=20
     * Search or list patients for a clinic.
     *
     * q: optional search term — matches name, phone, or email (case-insensitive)
     * activeOnly: true (default) hides inactive patients
     */
    @GetMapping("/api/v1/clinics/{clinicId}/patients")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PatientResponse>> listPatients(
            @PathVariable Long clinicId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable) {
        return ResponseEntity.ok(patientService.searchPatients(clinicId, q, activeOnly, pageable));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/patients/{patientId}
     * Get full patient profile including medical info, emergency contact, and notes.
     */
    @GetMapping("/api/v1/clinics/{clinicId}/patients/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PatientResponse> getPatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatientById(clinicId, patientId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/patients/{patientId}
     * Update patient details (patch semantics — only non-null fields are applied).
     */
    @PutMapping("/api/v1/clinics/{clinicId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @Valid @RequestBody UpdatePatientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(patientService.updatePatient(clinicId, patientId, request, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/patients/{patientId}
     * Soft-delete a patient. Medical records are preserved for legal compliance.
     */
    @DeleteMapping("/api/v1/clinics/{clinicId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserPrincipal principal) {
        patientService.deletePatient(clinicId, patientId, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // User account linking (for patient self-service portal)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * PUT /api/v1/clinics/{clinicId}/patients/{patientId}/link-user/{userId}
     * Link a registered user account to a patient profile.
     * Once linked, the patient can log in and view their own record via GET /patients/me.
     */
    @PutMapping("/api/v1/clinics/{clinicId}/patients/{patientId}/link-user/{userId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> linkUser(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(patientService.linkUserAccount(clinicId, patientId, userId, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/patients/{patientId}/link-user
     * Remove the user-patient link. The user account is not deleted.
     */
    @DeleteMapping("/api/v1/clinics/{clinicId}/patients/{patientId}/link-user")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> unlinkUser(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(patientService.unlinkUserAccount(clinicId, patientId, principal));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Patient self-view
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/patients/me
     * A logged-in patient views their own profile.
     * Finds the patient record linked to the caller's user account.
     * Returns 404 if the user account has no linked patient profile.
     */
    @GetMapping("/api/v1/patients/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PatientResponse> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(patientService.getMyProfile(principal));
    }
}
