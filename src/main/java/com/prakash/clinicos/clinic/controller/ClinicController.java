package com.prakash.clinicos.clinic.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.clinic.dto.request.*;
import com.prakash.clinicos.clinic.dto.response.*;
import com.prakash.clinicos.clinic.service.ClinicService;
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

import java.time.DayOfWeek;
import java.util.List;

@Tag(name = "Clinics", description = "Manage clinic profiles, business hours, closure dates, settings, and emergency close/reopen.")
@RestController
@RequestMapping("/api/v1/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // ── Clinic CRUD ───────────────────────────────────────────────────────────

    /**
     * Create a new clinic.
     * Only CLINIC_ADMIN can create; one admin = one clinic.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ClinicResponse> createClinic(
            @Valid @RequestBody CreateClinicRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicService.createClinic(req, principal));
    }

    /**
     * Get the authenticated CLINIC_ADMIN's own clinic.
     * Convenient shortcut — no need to know the clinic ID.
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ClinicResponse> getMyClinic(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.getMyClinic(principal));
    }

    /**
     * Public paginated list of all active clinics.
     * Optional ?city=Mumbai filter.
     * No auth required (patients / public portal use this).
     */
    @GetMapping
    public ResponseEntity<Page<ClinicResponse>> listClinics(
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(clinicService.listClinics(city, pageable));
    }

    /**
     * Get a single clinic by ID (public).
     * Returns full details including business hours, upcoming closures, settings.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClinicResponse> getClinicById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.getClinicById(id));
    }

    /**
     * Update clinic profile (partial update — only supplied fields change).
     * CLINIC_ADMIN can only update their own clinic; SUPER_ADMIN can update any.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ClinicResponse> updateClinic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClinicRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.updateClinic(id, req, principal));
    }

    /**
     * Soft-delete a clinic.
     * CLINIC_ADMIN can only delete their own; SUPER_ADMIN can delete any.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteClinic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        clinicService.deleteClinic(id, principal);
        return ResponseEntity.noContent().build();
    }

    // ── Emergency Controls ────────────────────────────────────────────────────

    /**
     * Immediately close clinic for emergencies (override all hours).
     * Stores reason + timestamp + who closed.
     */
    @PostMapping("/{id}/emergency-close")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ClinicResponse> emergencyClose(
            @PathVariable Long id,
            @Valid @RequestBody EmergencyCloseRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.emergencyClose(id, req, principal));
    }

    /**
     * Lift the emergency closure — clinic returns to its normal schedule.
     */
    @PostMapping("/{id}/emergency-reopen")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ClinicResponse> emergencyReopen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.emergencyReopen(id, principal));
    }

    // ── Business Hours ────────────────────────────────────────────────────────

    /**
     * Get all business hours for a clinic (public).
     * Returns every shift row sorted by day then open time.
     */
    @GetMapping("/{id}/hours")
    public ResponseEntity<List<BusinessHoursResponse>> getBusinessHours(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.getBusinessHours(id));
    }

    /**
     * Replace ALL business hours for the week in one call.
     * Existing hours are deleted and replaced with the submitted list.
     * Supports multiple shifts per day (e.g. morning + evening).
     * Validates: close > open, no overlapping shifts per day, max 6 shifts/day.
     */
    @PutMapping("/{id}/hours")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BusinessHoursResponse>> updateAllHours(
            @PathVariable Long id,
            @Valid @RequestBody List<DayShiftRequest> shifts,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.updateAllHours(id, shifts, principal));
    }

    /**
     * Replace shifts for a single day only (other days untouched).
     * {day} must be an IANA DayOfWeek name: MONDAY, TUESDAY, ... SUNDAY
     * Send an empty array [] to mark the clinic as closed that day.
     */
    @PutMapping("/{id}/hours/{day}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BusinessHoursResponse>> updateDayHours(
            @PathVariable Long id,
            @PathVariable DayOfWeek day,
            @Valid @RequestBody List<ShiftRequest> shifts,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.updateDayHours(id, day, shifts, principal));
    }

    /**
     * Remove all shifts for a specific day — clinic is closed that day.
     * Equivalent to PUT /hours/{day} with an empty array, but more explicit.
     */
    @DeleteMapping("/{id}/hours/{day}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeDayHours(
            @PathVariable Long id,
            @PathVariable DayOfWeek day,
            @AuthenticationPrincipal UserPrincipal principal) {
        clinicService.removeDayHours(id, day, principal);
        return ResponseEntity.noContent().build();
    }

    // ── Closure Dates ─────────────────────────────────────────────────────────

    /**
     * List planned closure dates for a clinic.
     * ?upcoming=true (default) → only today and future dates.
     * ?upcoming=false → all historical closure dates too.
     */
    @GetMapping("/{id}/closures")
    public ResponseEntity<List<ClosureDateResponse>> getClosureDates(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean upcoming) {
        return ResponseEntity.ok(clinicService.getClosureDates(id, upcoming));
    }

    /**
     * Add a planned closure date (e.g. national holiday, staff training).
     * Duplicate dates for the same clinic are rejected (409 Conflict).
     */
    @PostMapping("/{id}/closures")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ClosureDateResponse> addClosureDate(
            @PathVariable Long id,
            @Valid @RequestBody ClosureDateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicService.addClosureDate(id, req, principal));
    }

    /**
     * Remove a planned closure date by its ID.
     * Use this to cancel a previously planned day off.
     */
    @DeleteMapping("/{id}/closures/{closureId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeClosureDate(
            @PathVariable Long id,
            @PathVariable Long closureId,
            @AuthenticationPrincipal UserPrincipal principal) {
        clinicService.removeClosureDate(id, closureId, principal);
        return ResponseEntity.noContent().build();
    }

    // ── Clinic Settings ───────────────────────────────────────────────────────

    /**
     * Get appointment configuration for a clinic (public).
     * e.g. appointment slot duration, advance booking window.
     */
    @GetMapping("/{id}/settings")
    public ResponseEntity<ClinicSettingsResponse> getSettings(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.getSettings(id));
    }

    /**
     * Update appointment configuration.
     * All fields are required (full replace, not partial).
     */
    @PutMapping("/{id}/settings")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ClinicSettingsResponse> updateSettings(
            @PathVariable Long id,
            @Valid @RequestBody ClinicSettingsRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clinicService.updateSettings(id, req, principal));
    }
}
