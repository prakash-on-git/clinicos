package com.prakash.clinicos.medical.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.medical.dto.request.SaveClinicalNoteRequest;
import com.prakash.clinicos.medical.dto.response.ClinicalNoteResponse;
import com.prakash.clinicos.medical.service.ClinicalNoteService;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for SOAP Clinical Notes.
 *
 * Write endpoints: CLINIC_ADMIN / SUPER_ADMIN
 * Read endpoints:  any authenticated user
 */
@Tag(name = "Clinical Notes", description = "Record and retrieve doctor clinical notes linked to appointments.")
@RestController
public class ClinicalNoteController {

    private final ClinicalNoteService noteService;

    public ClinicalNoteController(ClinicalNoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * POST /api/v1/clinics/{clinicId}/appointments/{appointmentId}/notes
     *
     * Creates or updates the SOAP note for an appointment.
     * A null field leaves the existing value unchanged.
     * An empty string ("") clears a field.
     * One note per appointment — subsequent calls update in place.
     */
    @PostMapping("/api/v1/clinics/{clinicId}/appointments/{appointmentId}/notes")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ClinicalNoteResponse> save(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId,
            @RequestBody SaveClinicalNoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(noteService.save(clinicId, appointmentId, request, principal));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}/notes
     *
     * Get the SOAP note for a specific appointment.
     */
    @GetMapping("/api/v1/clinics/{clinicId}/appointments/{appointmentId}/notes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClinicalNoteResponse> getByAppointment(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(noteService.getByAppointment(clinicId, appointmentId));
    }
}
