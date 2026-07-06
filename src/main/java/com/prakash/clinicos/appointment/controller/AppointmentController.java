package com.prakash.clinicos.appointment.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.appointment.dto.request.*;
import com.prakash.clinicos.appointment.dto.response.AppointmentResponse;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import com.prakash.clinicos.appointment.service.AppointmentService;
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

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the Appointment module.
 *
 * Base path: /api/v1/clinics/{clinicId}/appointments
 *
 * Security:
 *   All appointment endpoints require authentication.
 *   Write endpoints (book, cancel, reschedule, status update) require CLINIC_ADMIN or SUPER_ADMIN.
 *   Read endpoints require any valid JWT.
 */
@Tag(name = "Appointments", description = "Book, reschedule, cancel, and track appointments with slot validation and double-booking prevention.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Book
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/clinics/{clinicId}/appointments
     *
     * Books an appointment after full validation:
     * - Doctor is active and belongs to this clinic
     * - Patient belongs to this clinic
     * - Date is today or future
     * - Doctor is available (not on leave, clinic not closed, working that day)
     * - Slot is free (not in break, not already booked)
     * - Appointment duration fits within working hours
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @PathVariable Long clinicId,
            @Valid @RequestBody BookAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.bookAppointment(clinicId, request, principal));
    }

    // ════════════════════════════════════════════════════════════════════════
    // List / Search
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/appointments
     *
     * List appointments with optional filters:
     *   ?doctorId=    – filter by doctor
     *   ?patientId=   – filter by patient
     *   ?date=        – filter by date (yyyy-MM-dd)
     *   ?status=      – filter by status (PENDING, CONFIRMED, etc.)
     *   &page=&size=&sort=appointmentDate,startTime
     *
     * All filters are optional and can be combined.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AppointmentResponse>> listAppointments(
            @PathVariable Long clinicId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(appointmentService.searchAppointments(
                clinicId, doctorId, patientId, date, status, pageable));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}
     * Get a single appointment with full detail (doctor name, patient name, treatment name).
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getById(clinicId, appointmentId));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Status updates
    // ════════════════════════════════════════════════════════════════════════

    /**
     * PATCH /api/v1/clinics/{clinicId}/appointments/{appointmentId}/status
     *
     * Advance the appointment lifecycle:
     *   PENDING    → CONFIRMED   (receptionist confirms)
     *   CONFIRMED  → IN_PROGRESS (doctor starts seeing patient)
     *   CONFIRMED  → COMPLETED   (skip IN_PROGRESS if needed)
     *   IN_PROGRESS → COMPLETED  (consultation finished)
     *   CONFIRMED  → NO_SHOW    (patient didn't arrive)
     *
     * For CANCELLED use the /cancel endpoint.
     * For RESCHEDULED use the /reschedule endpoint.
     */
    @PatchMapping("/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                appointmentService.updateStatus(clinicId, appointmentId, request, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/appointments/{appointmentId}/cancel
     * Cancel an appointment and free the slot.
     */
    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId,
            @RequestBody(required = false) CancelAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CancelAppointmentRequest req = request != null ? request : new CancelAppointmentRequest();
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(clinicId, appointmentId, req, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/appointments/{appointmentId}/reschedule
     *
     * Reschedule an appointment to a new date/time.
     * The old appointment is marked RESCHEDULED; a new PENDING appointment is created.
     * Returns the new appointment.
     */
    @PatchMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                appointmentService.rescheduleAppointment(clinicId, appointmentId, request, principal));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Doctor day schedule & patient history
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/appointments/doctor/{doctorId}/day?date=yyyy-MM-dd
     *
     * Returns all appointments for a doctor on a specific date, ordered by start time.
     * Includes all statuses (PENDING, CONFIRMED, COMPLETED, etc.) so the receptionist
     * can see the full picture including cancelled slots.
     */
    @GetMapping("/doctor/{doctorId}/day")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentResponse>> getDoctorDaySchedule(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                appointmentService.getDoctorDaySchedule(clinicId, doctorId, date));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/appointments/patient/{patientId}/history
     *
     * Returns a patient's full appointment history for this clinic,
     * most recent first. Useful for the receptionist "patient card" view.
     */
    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentResponse>> getPatientHistory(
            @PathVariable Long clinicId,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                appointmentService.getPatientHistory(clinicId, patientId));
    }
}
