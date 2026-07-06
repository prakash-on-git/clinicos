package com.prakash.clinicos.doctor.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** All fields optional — only non-null values are applied (partial update). */
@Getter
@Setter
public class UpdateTreatmentTypeRequest {

    @Size(min = 1, max = 255)
    private String name;

    private String description;

    /** Must be >= 10 and multiple of 10 if provided. Validated in service. */
    @Min(value = 10, message = "Duration must be at least 10 minutes")
    private Integer defaultDurationMins;

    @DecimalMin("0.00")
    private BigDecimal defaultFee;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "colorHex must be a valid hex color e.g. '#3B82F6'")
    private String colorHex;

    /** Toggle active/inactive. null = no change. */
    private Boolean active;
}
