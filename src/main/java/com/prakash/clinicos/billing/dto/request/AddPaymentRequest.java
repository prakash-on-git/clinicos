package com.prakash.clinicos.billing.dto.request;

import com.prakash.clinicos.billing.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AddPaymentRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    /** Defaults to today if omitted. */
    private LocalDate paymentDate;

    /** UPI transaction ID, card auth code, cheque number, etc. */
    private String transactionReference;

    private String notes;

    public BigDecimal getAmount()               { return amount; }
    public PaymentMethod getPaymentMethod()     { return paymentMethod; }
    public LocalDate getPaymentDate()           { return paymentDate; }
    public String getTransactionReference()     { return transactionReference; }
    public String getNotes()                    { return notes; }
}
