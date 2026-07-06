package com.prakash.clinicos.medical.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.medical.dto.request.RecordVitalsRequest;
import com.prakash.clinicos.medical.dto.response.VitalsResponse;
import com.prakash.clinicos.medical.service.VitalsService;
import com.prakash.clinicos.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Vital Signs.
 *
 * Write endpoints: CLINIC_ADMIN / SUPER_ADMIN
 * Read endpoints:  any authenticated user
 */
@Tag(name = "Vitals", description = "Record patient vital signs (BP, pulse, temperature, weight) per appointment.")
@RestController
public class VitalsController {

    private final VitalsService vitalsService;

    public VitalsController(VitalsService vitalsService) {
        this.vitalsService = vitalsService;
    }

    /**
     * POST /api/v1/clinics/{clinicId}/appointments/{appointmentId}/vitals
     *
     * Records (or updates) vital signs for an appointment.
     * Subsequent calls overwrite the previous vitals — one record per appointment.
     */
    @PostMapping("/api/v1/clinics/{clinicId}/appointments/{appointmentId}/vitals")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<VitalsResponse> record(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody RecordVitalsRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(vitalsService.record(clinicId, appointmentId, request, principal));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}/vitals
     *
     * Get vitals recorded for a specific appointment.
     */
    @GetMapping("/api/v1/clinics/{clinicId}/appointments/{appointmentId}/vitals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VitalsResponse> getByAppointment(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(vitalsService.getByAppointment(clinicId, appointmentId));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/patients/{patientId}/vitals
     *
     * Paginated vitals history for a patient — useful for trend views.
     * Ordered by recordedAt descending (most recent first).
     */
    @GetMapping("/api/v1/clinics/{clinicId}/patients/{patientId}/vitals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<VitalsResponse>> getByPatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(vitalsService.getByPatient(clinicId, patientId, pageable));
    }
}
