package com.prakash.clinicos.reporting.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.reporting.dto.response.*;
import com.prakash.clinicos.reporting.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for Reporting & Analytics.
 *
 * Base path: /api/v1/clinics/{clinicId}/reports
 *
 * All report endpoints are read-only and require CLINIC_ADMIN or SUPER_ADMIN.
 * Date range defaults: fromDate defaults to first day of current month,
 * toDate defaults to today.
 */
@Tag(name = "Reports", description = "Revenue, appointment, queue, patient, and doctor performance analytics.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * GET /api/v1/clinics/{clinicId}/reports/revenue
     *
     * Revenue summary for a date range:
     * - Total invoiced, collected, outstanding, refunded
     * - Invoice count
     * - Breakdown by doctor
     * - Breakdown by payment method
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RevenueReportResponse> revenue(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = toDate   != null ? toDate   : LocalDate.now();
        return ResponseEntity.ok(reportingService.revenueReport(clinicId, from, to));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/reports/appointments
     *
     * Appointment statistics for a date range:
     * - Counts by status (completed, cancelled, no-show, etc.)
     * - Completion rate
     * - Breakdown by doctor
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AppointmentReportResponse> appointments(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = toDate   != null ? toDate   : LocalDate.now();
        return ResponseEntity.ok(reportingService.appointmentReport(clinicId, from, to));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/reports/queue
     *
     * Queue performance for a date range:
     * - Token counts by status
     * - Average patient wait time and consultation duration
     * - Skip rate
     * - Breakdown by doctor
     */
    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueReportResponse> queue(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = toDate   != null ? toDate   : LocalDate.now();
        return ResponseEntity.ok(reportingService.queueReport(clinicId, from, to));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/reports/patients
     *
     * Patient statistics:
     * - Total active patients (all time)
     * - New registrations in the date range
     * - Gender distribution (all time)
     */
    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientReportResponse> patients(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = toDate   != null ? toDate   : LocalDate.now();
        return ResponseEntity.ok(reportingService.patientReport(clinicId, from, to));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/reports/doctors/{doctorId}
     *
     * Per-doctor performance summary:
     * - Appointment counts and completion
     * - Revenue generated and collected
     * - Average wait and consultation time (from queue data)
     * - Top 5 treatments performed
     */
    @GetMapping("/doctors/{doctorId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorPerformanceResponse> doctorPerformance(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to   = toDate   != null ? toDate   : LocalDate.now();
        return ResponseEntity.ok(reportingService.doctorPerformance(clinicId, doctorId, from, to));
    }
}
