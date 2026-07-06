package com.prakash.clinicos.doctor.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.doctor.dto.request.*;
import com.prakash.clinicos.doctor.dto.response.*;
import com.prakash.clinicos.doctor.service.DoctorAvailabilityService;
import com.prakash.clinicos.doctor.service.DoctorScheduleService;
import com.prakash.clinicos.doctor.service.DoctorService;
import com.prakash.clinicos.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the Doctor module.
 *
 * URL structure:
 *   /api/v1/clinics/{clinicId}/doctors        – Doctor CRUD + nested resources
 *   /api/v1/clinics/{clinicId}/treatments     – Clinic-level treatment type catalog
 *
 * Security:
 *   All GET endpoints are public (covered by SecurityConfig's permitAll for GET /api/v1/clinics/**).
 *   All write endpoints require CLINIC_ADMIN or SUPER_ADMIN.
 */
@Tag(name = "Doctors", description = "Manage doctor profiles, schedules, breaks, leaves, treatment types, and availability.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorScheduleService scheduleService;
    private final DoctorAvailabilityService availabilityService;

    public DoctorController(DoctorService doctorService,
                            DoctorScheduleService scheduleService,
                            DoctorAvailabilityService availabilityService) {
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
        this.availabilityService = availabilityService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Doctor CRUD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/clinics/{clinicId}/doctors
     * Register a new doctor under this clinic.
     */
    @PostMapping("/doctors")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreateDoctorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.createDoctor(clinicId, request, principal));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/doctors?activeOnly=true&page=0&size=20
     * List doctors in a clinic. activeOnly=true (default) hides inactive doctors.
     */
    @GetMapping("/doctors")
    public ResponseEntity<Page<DoctorResponse>> listDoctors(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return ResponseEntity.ok(doctorService.listDoctorsByClinic(clinicId, activeOnly, pageable));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}
     * Get full doctor profile including schedule, breaks, and treatments.
     */
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorResponse> getDoctor(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}
     * Update doctor profile fields.
     */
    @PutMapping("/doctors/{doctorId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @Valid @RequestBody UpdateDoctorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.updateDoctor(clinicId, doctorId, request, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}
     * Soft-delete a doctor.
     */
    @DeleteMapping("/doctors/{doctorId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        doctorService.deleteDoctor(clinicId, doctorId, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // User account linking
    // ════════════════════════════════════════════════════════════════════════

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/link-user/{userId}
     * Link a user account to a doctor profile (so the doctor can log in).
     */
    @PutMapping("/doctors/{doctorId}/link-user/{userId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> linkUser(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.linkUserAccount(clinicId, doctorId, userId, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/link-user
     * Unlink the user account from this doctor profile.
     */
    @DeleteMapping("/doctors/{doctorId}/link-user")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> unlinkUser(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.unlinkUserAccount(clinicId, doctorId, principal));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Weekly Schedule
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule
     * Get all weekly schedule shifts for a doctor.
     */
    @GetMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<List<DoctorWeeklyScheduleResponse>> getSchedule(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getSchedule(doctorId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule
     * Replace the entire weekly schedule (all days) for a doctor.
     * Sends the full new schedule; anything not included is deleted.
     */
    @PutMapping("/doctors/{doctorId}/schedule")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<DoctorWeeklyScheduleResponse>> replaceFullSchedule(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @Valid @RequestBody List<DayScheduleRequest> shifts,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(scheduleService.replaceFullSchedule(doctorId, shifts, principal));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule/{day}
     * Replace shifts for a single day of the week. Other days are untouched.
     * day = MONDAY, TUESDAY, ... (case-insensitive)
     */
    @PutMapping("/doctors/{doctorId}/schedule/{day}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<DoctorWeeklyScheduleResponse>> replaceDaySchedule(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable DayOfWeek day,
            @Valid @RequestBody List<DayScheduleRequest> shifts,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(scheduleService.replaceDaySchedule(doctorId, day, shifts, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule/{day}
     * Remove all shifts for a day — doctor doesn't work that weekday at all.
     * Also clears all breaks for that day.
     */
    @DeleteMapping("/doctors/{doctorId}/schedule/{day}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> clearDaySchedule(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable DayOfWeek day,
            @AuthenticationPrincipal UserPrincipal principal) {
        scheduleService.clearDaySchedule(doctorId, day, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Recurring Breaks (lunch, tea, prayer, etc.)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/breaks
     * Get all recurring breaks for a doctor (all days).
     */
    @GetMapping("/doctors/{doctorId}/breaks")
    public ResponseEntity<List<DoctorBreakResponse>> getBreaks(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getBreaks(doctorId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/breaks/{day}
     * Replace all recurring breaks for a specific day.
     * Breaks must fall within the doctor's working window for that day.
     * All times must align to 10-minute boundaries.
     */
    @PutMapping("/doctors/{doctorId}/breaks/{day}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<DoctorBreakResponse>> replaceDayBreaks(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable DayOfWeek day,
            @Valid @RequestBody List<DoctorBreakRequest> breaks,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(scheduleService.replaceDayBreaks(doctorId, day, breaks, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/breaks/{day}
     * Remove all recurring breaks for a specific day.
     */
    @DeleteMapping("/doctors/{doctorId}/breaks/{day}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> clearDayBreaks(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable DayOfWeek day,
            @AuthenticationPrincipal UserPrincipal principal) {
        scheduleService.clearDayBreaks(doctorId, day, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Day Overrides (late start, early end, day off, custom hours)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/overrides
     * List all upcoming (today or future) day overrides for a doctor.
     */
    @GetMapping("/doctors/{doctorId}/overrides")
    public ResponseEntity<List<DoctorDayOverrideResponse>> getOverrides(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorService.getUpcomingOverrides(clinicId, doctorId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/overrides
     * Create or update a day override. The target date comes from the request body (overrideDate).
     *
     * Override types:
     *   DAY_OFF       — no startTime/endTime; doctor won't have any slots that day
     *   LATE_START    — startTime required; doctor's first slot shifts forward
     *   EARLY_END     — endTime required; doctor's last slot shifts back
     *   CUSTOM_HOURS  — both startTime and endTime; fully replaces that day's schedule
     */
    @PutMapping("/doctors/{doctorId}/overrides")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorDayOverrideResponse> addOrUpdateOverride(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorDayOverrideRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.addOrUpdateOverride(clinicId, doctorId, request, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/overrides/{date}
     * Remove a day override — the doctor reverts to their normal weekly schedule.
     */
    @DeleteMapping("/doctors/{doctorId}/overrides/{date}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeOverride(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal principal) {
        doctorService.removeOverride(clinicId, doctorId, date, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Leave Management
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/leave?upcomingOnly=true
     * Get leave dates for a doctor. upcomingOnly=true (default) shows only today + future dates.
     */
    @GetMapping("/doctors/{doctorId}/leave")
    public ResponseEntity<List<DoctorLeaveResponse>> getLeave(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "true") boolean upcomingOnly) {
        return ResponseEntity.ok(doctorService.getLeave(clinicId, doctorId, upcomingOnly));
    }

    /**
     * POST /api/v1/clinics/{clinicId}/doctors/{doctorId}/leave
     * Add leave dates for a doctor.
     *
     * Accepts a list of dates so you can mark a 2-week vacation in one call.
     * If some dates already exist, they are skipped (partial success).
     * If ALL dates already exist, returns 409.
     */
    @PostMapping("/doctors/{doctorId}/leave")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<DoctorLeaveResponse>> addLeave(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorLeaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.addLeave(clinicId, doctorId, request, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/leave/{date}
     * Remove a single leave date (e.g. doctor recovered early from sick leave).
     */
    @DeleteMapping("/doctors/{doctorId}/leave/{date}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeLeave(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal principal) {
        doctorService.removeLeave(clinicId, doctorId, date, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Availability (slot computation)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/availability?date=yyyy-MM-dd
     * Compute all available 10-minute slots for a doctor on a specific date.
     *
     * Checks (in order): doctor active, clinic open, clinic closure date,
     * doctor leave, day override, weekly schedule, clinic hours intersection,
     * recurring breaks. Returns all free slot start times in "HH:mm" format.
     */
    @GetMapping("/doctors/{doctorId}/availability")
    public ResponseEntity<DoctorAvailabilityResponse> getAvailability(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.computeAvailability(doctorId, date));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Treatment Types (clinic-level catalog)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/clinics/{clinicId}/treatments
     * Create a new treatment type in the clinic's catalog.
     * Duration must be a multiple of 10 minutes (e.g. 10, 20, 30, 60).
     */
    @PostMapping("/treatments")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TreatmentTypeResponse> createTreatment(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreateTreatmentTypeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.createTreatment(clinicId, request, principal));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/treatments?activeOnly=true
     * List treatment types in the clinic's catalog. activeOnly=true (default) hides inactive ones.
     */
    @GetMapping("/treatments")
    public ResponseEntity<List<TreatmentTypeResponse>> listTreatments(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(doctorService.listTreatments(clinicId, activeOnly));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/treatments/{treatmentId}
     * Get a single treatment type by ID.
     */
    @GetMapping("/treatments/{treatmentId}")
    public ResponseEntity<TreatmentTypeResponse> getTreatment(
            @PathVariable Long clinicId,
            @PathVariable Long treatmentId) {
        return ResponseEntity.ok(doctorService.getTreatment(clinicId, treatmentId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/treatments/{treatmentId}
     * Update a treatment type.
     */
    @PutMapping("/treatments/{treatmentId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TreatmentTypeResponse> updateTreatment(
            @PathVariable Long clinicId,
            @PathVariable Long treatmentId,
            @Valid @RequestBody UpdateTreatmentTypeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(doctorService.updateTreatment(clinicId, treatmentId, request, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/treatments/{treatmentId}
     * Soft-delete a treatment type from the catalog.
     */
    @DeleteMapping("/treatments/{treatmentId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTreatment(
            @PathVariable Long clinicId,
            @PathVariable Long treatmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        doctorService.deleteTreatment(clinicId, treatmentId, principal);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Doctor Treatments (per-doctor treatment assignments with overrides)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/treatments
     * Get all treatments assigned to a doctor, with their effective duration and fee.
     */
    @GetMapping("/doctors/{doctorId}/treatments")
    public ResponseEntity<List<DoctorTreatmentResponse>> getDoctorTreatments(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorTreatments(clinicId, doctorId));
    }

    /**
     * PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/treatments/{treatmentId}
     * Assign a treatment type to a doctor, optionally overriding the default
     * duration and/or fee for this doctor specifically.
     *
     * If the assignment already exists, it is updated (upsert).
     * If customDurationMins is provided, it must be a multiple of 10.
     */
    @PutMapping("/doctors/{doctorId}/treatments/{treatmentId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorTreatmentResponse> addOrUpdateDoctorTreatment(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable Long treatmentId,
            @Valid @RequestBody DoctorTreatmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                doctorService.addOrUpdateDoctorTreatment(clinicId, doctorId, treatmentId, request, principal));
    }

    /**
     * DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/treatments/{treatmentId}
     * Remove a treatment assignment from a doctor.
     */
    @DeleteMapping("/doctors/{doctorId}/treatments/{treatmentId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeDoctorTreatment(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @PathVariable Long treatmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        doctorService.removeDoctorTreatment(clinicId, doctorId, treatmentId, principal);
        return ResponseEntity.noContent().build();
    }
}
