package com.prakash.clinicos.medical.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.medical.dto.request.CreatePrescriptionRequest;
import com.prakash.clinicos.medical.dto.request.UpdatePrescriptionRequest;
import com.prakash.clinicos.medical.dto.response.PrescriptionResponse;
import com.prakash.clinicos.medical.service.PrescriptionService;
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
 * REST controller for Prescriptions.
 *
 * Write endpoints: CLINIC_ADMIN / SUPER_ADMIN
 * Read endpoints:  any authenticated user
 */
@Tag(name = "Prescriptions", description = "Create and manage prescriptions with multiple medicines per appointment.")
@RestController
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * POST /api/v1/clinics/{clinicId}/prescriptions
     *
     * Creates a new prescription, optionally linked to an appointment.
     * Medicines are included in the same request.
     */
    @PostMapping("/api/v1/clinics/{clinicId}/prescriptions")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PrescriptionResponse> create(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreatePrescriptionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(clinicId, request, principal));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/prescriptions/{prescriptionId}
     *
     * Updates an existing prescription.
     * Providing a medicines list replaces all existing medicines.
     * Omitting medicines (null) leaves them unchanged.
     */
    @PutMapping("/api/v1/clinics/{clinicId}/prescriptions/{prescriptionId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PrescriptionResponse> update(
            @PathVariable Long clinicId,
            @PathVariable Long prescriptionId,
            @Valid @RequestBody UpdatePrescriptionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(prescriptionService.update(clinicId, prescriptionId, request, principal));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/prescriptions/{prescriptionId}
     *
     * Full prescription detail including all medicines.
     */
    @GetMapping("/api/v1/clinics/{clinicId}/prescriptions/{prescriptionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrescriptionResponse> getById(
            @PathVariable Long clinicId,
            @PathVariable Long prescriptionId) {
        return ResponseEntity.ok(prescriptionService.getById(clinicId, prescriptionId));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}/prescription
     *
     * Get the prescription written for a specific appointment.
     */
    @GetMapping("/api/v1/clinics/{clinicId}/appointments/{appointmentId}/prescription")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrescriptionResponse> getByAppointment(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(prescriptionService.getByAppointment(clinicId, appointmentId));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/patients/{patientId}/prescriptions
     *
     * Full prescription history for a patient, newest first.
     */
    @GetMapping("/api/v1/clinics/{clinicId}/patients/{patientId}/prescriptions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PrescriptionResponse>> getByPatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getByPatient(clinicId, patientId, pageable));
    }
}
