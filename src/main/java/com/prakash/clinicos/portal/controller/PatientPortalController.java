package com.prakash.clinicos.portal.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.appointment.dto.response.AppointmentResponse;
import com.prakash.clinicos.billing.dto.response.InvoiceResponse;
import com.prakash.clinicos.medical.dto.response.PrescriptionResponse;
import com.prakash.clinicos.portal.dto.request.PatientNotificationPreferencesRequest;
import com.prakash.clinicos.portal.dto.response.PatientProfileResponse;
import com.prakash.clinicos.portal.service.PatientPortalService;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Patient self-service endpoints — all require PATIENT role.
 *
 * The authenticated patient can only ever see their own data.
 * The patient record is resolved from the JWT's userId, so no
 * patientId path variable is needed (and no spoofing is possible).
 */
@Tag(name = "Patient Portal", description = "Patient self-service - view profile, appointments, prescriptions, invoices.")
@RestController
@RequestMapping("/api/v1/me")
@PreAuthorize("hasRole('PATIENT')")
public class PatientPortalController {

    private final PatientPortalService portalService;

    public PatientPortalController(PatientPortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/profile")
    public ResponseEntity<PatientProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(portalService.getMyProfile(principal.getId()));
    }

    @GetMapping("/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(portalService.getMyAppointments(principal.getId(), pageable));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<Page<PrescriptionResponse>> getPrescriptions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(portalService.getMyPrescriptions(principal.getId(), pageable));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceResponse>> getInvoices(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(portalService.getMyInvoices(principal.getId(), pageable));
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<PatientProfileResponse> updateNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PatientNotificationPreferencesRequest req) {
        return ResponseEntity.ok(
                portalService.updateNotificationPreferences(principal.getId(), req));
    }
}
