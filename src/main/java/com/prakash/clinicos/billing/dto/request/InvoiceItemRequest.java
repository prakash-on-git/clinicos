package com.prakash.clinicos.billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class InvoiceItemRequest {

    /** Optional back-reference to a treatment type (for reporting). */
    private Long treatmentTypeId;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", message = "unitPrice cannot be negative")
    private BigDecimal unitPrice;

    public Long getTreatmentTypeId()    { return treatmentTypeId; }
    public String getDescription()      { return description; }
    public BigDecimal getQuantity()     { return quantity; }
    public BigDecimal getUnitPrice()    { return unitPrice; }
}
