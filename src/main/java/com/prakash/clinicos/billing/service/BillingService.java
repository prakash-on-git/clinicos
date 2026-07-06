package com.prakash.clinicos.billing.service;

import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.billing.dto.request.AddPaymentRequest;
import com.prakash.clinicos.billing.dto.request.CancelInvoiceRequest;
import com.prakash.clinicos.billing.dto.request.CreateInvoiceRequest;
import com.prakash.clinicos.billing.dto.request.InvoiceItemRequest;
import com.prakash.clinicos.billing.dto.response.InvoiceItemResponse;
import com.prakash.clinicos.billing.dto.response.InvoiceResponse;
import com.prakash.clinicos.billing.dto.response.PaymentResponse;
import com.prakash.clinicos.billing.entity.*;
import com.prakash.clinicos.billing.repository.InvoiceItemRepository;
import com.prakash.clinicos.billing.repository.InvoiceRepository;
import com.prakash.clinicos.billing.repository.PaymentRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.notification.service.NotificationService;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class BillingService {

    private static final Set<InvoiceStatus> TERMINAL = Set.of(
            InvoiceStatus.PAID, InvoiceStatus.CANCELLED, InvoiceStatus.REFUNDED);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository itemRepository;
    private final PaymentRepository paymentRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public BillingService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository itemRepository,
                          PaymentRepository paymentRepository,
                          ClinicRepository clinicRepository,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          NotificationService notificationService,
                          AuditService auditService) {
        this.invoiceRepository = invoiceRepository;
        this.itemRepository = itemRepository;
        this.paymentRepository = paymentRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Create invoice
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public InvoiceResponse createInvoice(Long clinicId,
                                         CreateInvoiceRequest req,
                                         UserPrincipal principal) {
        // 1. Clinic exists
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));

        // 2. Doctor belongs to clinic
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Doctor does not belong to this clinic");
        }

        // 3. Patient belongs to clinic
        Patient patient = patientRepository.findByIdAndDeletedFalse(req.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (!patient.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Patient does not belong to this clinic");
        }

        // 4. Appointment validation (if linked)
        if (req.getAppointmentId() != null) {
            Appointment appt = appointmentRepository
                    .findByIdAndClinicId(req.getAppointmentId(), clinicId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found"));

            if (Set.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
                    .contains(appt.getStatus())) {
                throw new AppException(HttpStatus.CONFLICT,
                        "Cannot bill a cancelled or no-show appointment");
            }
            if (invoiceRepository.existsByAppointmentId(req.getAppointmentId())) {
                throw new AppException(HttpStatus.CONFLICT,
                        "An invoice already exists for this appointment");
            }
        }

        // 5. Mutually exclusive discount fields
        if (req.getDiscountPercent() != null && req.getDiscountAmount() != null
                && req.getDiscountPercent().compareTo(BigDecimal.ZERO) != 0
                && req.getDiscountAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Provide either discountPercent or discountAmount, not both");
        }

        // 6. Build items and compute subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        List<InvoiceItemRequest> itemRequests = req.getItems();
        for (InvoiceItemRequest item : itemRequests) {
            subtotal = subtotal.add(item.getQuantity().multiply(item.getUnitPrice()));
        }

        // 7. Discount
        BigDecimal discountPct = coalesce(req.getDiscountPercent());
        BigDecimal discountAmt;
        if (req.getDiscountPercent() != null && req.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountAmt = subtotal.multiply(discountPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmt = coalesce(req.getDiscountAmount());
            discountPct = BigDecimal.ZERO;
        }

        if (discountAmt.compareTo(subtotal) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Discount cannot exceed subtotal");
        }

        // 8. Tax
        BigDecimal taxPct = coalesce(req.getTaxPercent());
        BigDecimal taxableAmount = subtotal.subtract(discountAmt);
        BigDecimal taxAmt = taxableAmount.multiply(taxPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal total = taxableAmount.add(taxAmt);

        // 9. Invoice number: INV-YYYY-NNNNN
        int year = Year.now().getValue();
        int seq = invoiceRepository.getNextSequenceForYear(clinicId, year);
        String invoiceNumber = String.format("INV-%d-%05d", year, seq);

        LocalDate invoiceDate = req.getInvoiceDate() != null ? req.getInvoiceDate() : LocalDate.now();

        Invoice invoice = Invoice.builder()
                .clinicId(clinicId)
                .patientId(req.getPatientId())
                .doctorId(req.getDoctorId())
                .appointmentId(req.getAppointmentId())
                .invoiceNumber(invoiceNumber)
                .invoiceDate(invoiceDate)
                .dueDate(req.getDueDate())
                .status(InvoiceStatus.DRAFT)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountPercent(discountPct.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(discountAmt.setScale(2, RoundingMode.HALF_UP))
                .taxPercent(taxPct.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmt.setScale(2, RoundingMode.HALF_UP))
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .amountPaid(BigDecimal.ZERO)
                .amountDue(total.setScale(2, RoundingMode.HALF_UP))
                .notes(req.getNotes())
                .createdBy(principal.getId())
                .build();

        invoice = invoiceRepository.save(invoice);

        // 10. Persist items
        final Long invoiceId = invoice.getId();
        List<InvoiceItem> savedItems = itemRequests.stream()
                .map(r -> itemRepository.save(InvoiceItem.builder()
                        .invoiceId(invoiceId)
                        .treatmentTypeId(r.getTreatmentTypeId())
                        .description(r.getDescription())
                        .quantity(r.getQuantity().setScale(2, RoundingMode.HALF_UP))
                        .unitPrice(r.getUnitPrice().setScale(2, RoundingMode.HALF_UP))
                        .totalPrice(r.getQuantity().multiply(r.getUnitPrice())
                                .setScale(2, RoundingMode.HALF_UP))
                        .build()))
                .toList();

        InvoiceResponse created = toResponse(invoice, savedItems, List.of(), doctor, patient);
        auditService.log(clinicId, "INVOICE", invoice.getId(), AuditAction.CREATE, null, created, principal.getId());
        return created;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Get invoice
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long clinicId, Long invoiceId) {
        Invoice invoice = load(clinicId, invoiceId);
        return toFullResponse(invoice);
    }

    // ════════════════════════════════════════════════════════════════════════
    // List / search invoices
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> searchInvoices(Long clinicId,
                                                 Long patientId,
                                                 Long doctorId,
                                                 InvoiceStatus status,
                                                 LocalDate fromDate,
                                                 LocalDate toDate,
                                                 Pageable pageable) {
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));

        return invoiceRepository.search(clinicId, patientId, doctorId, status, fromDate, toDate, pageable)
                .map(this::toSummaryResponse);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Issue invoice (DRAFT → ISSUED)
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public InvoiceResponse issueInvoice(Long clinicId, Long invoiceId, UserPrincipal principal) {
        Invoice invoice = load(clinicId, invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Only DRAFT invoices can be issued; current status: " + invoice.getStatus());
        }

        InvoiceResponse before = toFullResponse(invoice);

        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedBy(principal.getId());
        invoice.setIssuedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        notificationService.notifyInvoiceIssued(clinicId, invoice.getId(),
                invoice.getPatientId(), invoice.getInvoiceNumber(), invoice.getTotalAmount());

        InvoiceResponse after = toFullResponse(invoice);
        auditService.log(clinicId, "INVOICE", invoiceId, AuditAction.UPDATE, before, after, principal.getId());
        return after;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Add payment
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public InvoiceResponse addPayment(Long clinicId,
                                      Long invoiceId,
                                      AddPaymentRequest req,
                                      UserPrincipal principal) {
        Invoice invoice = load(clinicId, invoiceId);

        InvoiceResponse beforePayment = toFullResponse(invoice);

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot pay a cancelled invoice");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(HttpStatus.CONFLICT, "Invoice is already fully paid");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Invoice must be ISSUED before payment can be recorded");
        }

        BigDecimal remaining = invoice.getAmountDue();
        if (req.getAmount().compareTo(remaining) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Payment amount (" + req.getAmount() + ") exceeds amount due (" + remaining + ")");
        }

        Payment payment = Payment.builder()
                .invoiceId(invoiceId)
                .clinicId(clinicId)
                .amount(req.getAmount().setScale(2, RoundingMode.HALF_UP))
                .paymentMethod(req.getPaymentMethod())
                .paymentDate(req.getPaymentDate() != null ? req.getPaymentDate() : LocalDate.now())
                .transactionReference(req.getTransactionReference())
                .notes(req.getNotes())
                .receivedBy(principal.getId())
                .build();

        paymentRepository.save(payment);

        // Recompute totals from DB sum to avoid drift
        BigDecimal totalPaid = paymentRepository.sumByInvoiceId(invoiceId);
        BigDecimal amountDue = invoice.getTotalAmount().subtract(totalPaid)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setAmountPaid(totalPaid.setScale(2, RoundingMode.HALF_UP));
        invoice.setAmountDue(amountDue);
        invoice.setStatus(amountDue.compareTo(BigDecimal.ZERO) == 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID);

        invoiceRepository.save(invoice);

        notificationService.notifyPaymentReceived(clinicId, invoice.getId(),
                invoice.getPatientId(), invoice.getInvoiceNumber(),
                req.getAmount(), amountDue);

        InvoiceResponse afterPayment = toFullResponse(invoice);
        auditService.log(clinicId, "INVOICE", invoiceId, AuditAction.UPDATE, beforePayment, afterPayment, principal.getId());
        return afterPayment;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Cancel invoice
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public InvoiceResponse cancelInvoice(Long clinicId,
                                          Long invoiceId,
                                          CancelInvoiceRequest req,
                                          UserPrincipal principal) {
        Invoice invoice = load(clinicId, invoiceId);

        if (TERMINAL.contains(invoice.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Cannot cancel an invoice with status: " + invoice.getStatus());
        }

        InvoiceResponse beforeCancel = toFullResponse(invoice);

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelledBy(principal.getId());
        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancellationReason(req != null ? req.getReason() : null);

        invoiceRepository.save(invoice);

        InvoiceResponse afterCancel = toFullResponse(invoice);
        auditService.log(clinicId, "INVOICE", invoiceId, AuditAction.UPDATE, beforeCancel, afterCancel, principal.getId());
        return afterCancel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Refund invoice (PAID/PARTIALLY_PAID → REFUNDED)
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public InvoiceResponse refundInvoice(Long clinicId, Long invoiceId, UserPrincipal principal) {
        Invoice invoice = load(clinicId, invoiceId);

        if (invoice.getStatus() != InvoiceStatus.PAID
                && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Only PAID or PARTIALLY_PAID invoices can be refunded; current: " + invoice.getStatus());
        }

        invoice.setStatus(InvoiceStatus.REFUNDED);
        invoiceRepository.save(invoice);

        return toFullResponse(invoice);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════════

    private Invoice load(Long clinicId, Long invoiceId) {
        return invoiceRepository.findByIdAndClinicId(invoiceId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Invoice not found for this clinic"));
    }

    private InvoiceResponse toFullResponse(Invoice invoice) {
        List<InvoiceItem> items = itemRepository.findByInvoiceIdOrderById(invoice.getId());
        List<Payment> payments = paymentRepository
                .findByInvoiceIdOrderByPaymentDateAscIdAsc(invoice.getId());

        Doctor doctor = doctorRepository.findById(invoice.getDoctorId()).orElse(null);
        Patient patient = patientRepository.findByIdAndDeletedFalse(invoice.getPatientId()).orElse(null);

        return toResponse(invoice, items, payments, doctor, patient);
    }

    private InvoiceResponse toSummaryResponse(Invoice invoice) {
        Doctor doctor = doctorRepository.findById(invoice.getDoctorId()).orElse(null);
        Patient patient = patientRepository.findByIdAndDeletedFalse(invoice.getPatientId()).orElse(null);
        return toResponse(invoice, null, null, doctor, patient);
    }

    private InvoiceResponse toResponse(Invoice invoice,
                                        List<InvoiceItem> items,
                                        List<Payment> payments,
                                        Doctor doctor,
                                        Patient patient) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .clinicId(invoice.getClinicId())
                .patientId(invoice.getPatientId())
                .doctorId(invoice.getDoctorId())
                .appointmentId(invoice.getAppointmentId())
                .patientName(patient != null
                        ? patient.getFirstName() + " " + patient.getLastName() : null)
                .doctorName(doctor != null ? "Dr. " + doctor.getFullName() : null)
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .discountPercent(invoice.getDiscountPercent())
                .discountAmount(invoice.getDiscountAmount())
                .taxPercent(invoice.getTaxPercent())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .amountPaid(invoice.getAmountPaid())
                .amountDue(invoice.getAmountDue())
                .notes(invoice.getNotes())
                .items(items == null ? null : items.stream().map(this::toItemResponse).toList())
                .payments(payments == null ? null : payments.stream().map(this::toPaymentResponse).toList())
                .issuedAt(invoice.getIssuedAt())
                .cancelledAt(invoice.getCancelledAt())
                .cancellationReason(invoice.getCancellationReason())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    private InvoiceItemResponse toItemResponse(InvoiceItem item) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .treatmentTypeId(item.getTreatmentTypeId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoiceId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionReference(payment.getTransactionReference())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private BigDecimal coalesce(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
