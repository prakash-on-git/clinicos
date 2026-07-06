package com.prakash.clinicos.doctor.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTreatmentTypeRequest {

    @NotBlank(message = "Treatment name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    private String description;

    /**
     * Duration in minutes. Must be >= 10 and a multiple of 10.
     * Examples: 10, 20, 30, 60, 90.
     * Enforced in service layer (no built-in @MultipleOf annotation in Jakarta Validation).
     */
    @NotNull(message = "defaultDurationMins is required")
    @Min(value = 10, message = "Duration must be at least 10 minutes")
    private Integer defaultDurationMins;

    @NotNull(message = "defaultFee is required")
    @DecimalMin(value = "0.00", message = "Fee cannot be negative")
    private BigDecimal defaultFee;

    /** Optional hex color for UI e.g. "#3B82F6". Pattern: # followed by 6 hex chars. */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "colorHex must be a valid hex color e.g. '#3B82F6'")
    private String colorHex;
}
