package com.prakash.clinicos.doctor.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Add or update a treatment for a doctor (upsert by treatmentTypeId).
 * Used in PUT /doctors/{id}/treatments/{treatmentTypeId}.
 *
 * If customDurationMins or customFee is null, the treatment type's defaults apply.
 * The effective duration and fee are computed and included in the response.
 */
@Getter
@Setter
public class DoctorTreatmentRequest {

    /**
     * Override per-doctor duration. Must be >= 10 and a multiple of 10 if provided.
     * null = use treatment type's defaultDurationMins.
     */
    @Min(value = 10, message = "Custom duration must be at least 10 minutes")
    private Integer customDurationMins;

    /**
     * Override per-doctor fee. null = use treatment type's defaultFee.
     */
    @DecimalMin(value = "0.00", message = "Custom fee cannot be negative")
    private BigDecimal customFee;

    /** Whether this doctor is currently offering this treatment. Default true. */
    private Boolean active;
}
