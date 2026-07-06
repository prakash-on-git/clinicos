package com.prakash.clinicos.billing.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.billing.dto.request.AddPaymentRequest;
import com.prakash.clinicos.billing.dto.request.CancelInvoiceRequest;
import com.prakash.clinicos.billing.dto.request.CreateInvoiceRequest;
import com.prakash.clinicos.billing.dto.response.InvoiceResponse;
import com.prakash.clinicos.billing.entity.InvoiceStatus;
import com.prakash.clinicos.billing.service.BillingService;
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

/**
 * REST controller for the Billing module.
 *
 * Base path: /api/v1/clinics/{clinicId}/billing
 *
 * All write operations require CLINIC_ADMIN or SUPER_ADMIN.
 * Read operations require any valid JWT.
 */
@Tag(name = "Billing", description = "Create invoices, add payments, and manage invoice lifecycle with discount and tax calculation.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Create invoice
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/clinics/{clinicId}/billing/invoices
     *
     * Creates a DRAFT invoice for a patient visit.
     * Optionally links to an existing appointment.
     * Invoice number is auto-generated: INV-{YEAR}-{SEQ}.
     */
    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> createInvoice(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.createInvoice(clinicId, request, principal));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Read invoices
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/billing/invoices
     *
     * Paginated invoice list. Optional filters: patientId, doctorId, status, fromDate, toDate.
     */
    @GetMapping("/invoices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(
            @PathVariable Long clinicId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
                billingService.searchInvoices(clinicId, patientId, doctorId, status, fromDate, toDate, pageable));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}
     *
     * Full invoice detail including all line items and payment history.
     */
    @GetMapping("/invoices/{invoiceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @PathVariable Long clinicId,
            @PathVariable Long invoiceId) {
        return ResponseEntity.ok(billingService.getInvoice(clinicId, invoiceId));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Status transitions
    // ════════════════════════════════════════════════════════════════════════

    /**
     * PATCH /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/issue
     *
     * DRAFT → ISSUED.
     * Finalizes the invoice and presents it to the patient.
     * No further item edits are allowed after this point.
     */
    @PatchMapping("/invoices/{invoiceId}/issue")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> issueInvoice(
            @PathVariable Long clinicId,
            @PathVariable Long invoiceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billingService.issueInvoice(clinicId, invoiceId, principal));
    }

    /**
     * POST /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/payments
     *
     * Records a payment against an ISSUED invoice.
     * Automatically transitions status to PARTIALLY_PAID or PAID.
     */
    @PostMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> addPayment(
            @PathVariable Long clinicId,
            @PathVariable Long invoiceId,
            @Valid @RequestBody AddPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billingService.addPayment(clinicId, invoiceId, request, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/cancel
     *
     * Cancels a DRAFT or ISSUED invoice.
     * Cannot cancel a PAID or already CANCELLED invoice.
     */
    @PatchMapping("/invoices/{invoiceId}/cancel")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> cancelInvoice(
            @PathVariable Long clinicId,
            @PathVariable Long invoiceId,
            @RequestBody(required = false) CancelInvoiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billingService.cancelInvoice(clinicId, invoiceId, request, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/refund
     *
     * Marks a PAID or PARTIALLY_PAID invoice as REFUNDED.
     * Use this when money has been returned to the patient.
     */
    @PatchMapping("/invoices/{invoiceId}/refund")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> refundInvoice(
            @PathVariable Long clinicId,
            @PathVariable Long invoiceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billingService.refundInvoice(clinicId, invoiceId, principal));
    }
}
