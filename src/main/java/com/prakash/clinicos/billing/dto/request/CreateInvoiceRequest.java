package com.prakash.clinicos.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateInvoiceRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    /** Optional: links invoice to a completed/in-progress appointment. */
    private Long appointmentId;

    /** Defaults to today if omitted. */
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @NotEmpty(message = "at least one item is required")
    @Valid
    private List<InvoiceItemRequest> items;

    /**
     * Percentage discount applied to subtotal before tax.
     * Mutually exclusive with discountAmount — send one or the other (or neither).
     */
    @DecimalMin(value = "0.0", message = "discountPercent cannot be negative")
    @DecimalMax(value = "100.0", message = "discountPercent cannot exceed 100")
    private BigDecimal discountPercent;

    /**
     * Fixed monetary discount.
     * Mutually exclusive with discountPercent.
     */
    @DecimalMin(value = "0.0", message = "discountAmount cannot be negative")
    private BigDecimal discountAmount;

    /**
     * GST / VAT percentage applied after discount.
     */
    @DecimalMin(value = "0.0", message = "taxPercent cannot be negative")
    private BigDecimal taxPercent;

    private String notes;

    public Long getPatientId()              { return patientId; }
    public Long getDoctorId()               { return doctorId; }
    public Long getAppointmentId()          { return appointmentId; }
    public LocalDate getInvoiceDate()       { return invoiceDate; }
    public LocalDate getDueDate()           { return dueDate; }
    public List<InvoiceItemRequest> getItems() { return items; }
    public BigDecimal getDiscountPercent()  { return discountPercent; }
    public BigDecimal getDiscountAmount()   { return discountAmount; }
    public BigDecimal getTaxPercent()       { return taxPercent; }
    public String getNotes()                { return notes; }
}
