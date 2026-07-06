package com.prakash.clinicos.queue.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Request to generate a walk-in queue token.
 *
 * Both patientId and doctorId are required for walk-ins.
 * For appointment check-ins use POST /queue/checkin instead.
 */
@Getter
public class GenerateTokenRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    /** Optional notes from receptionist at check-in (e.g. "Fever, cough since 2 days"). */
    private String notes;
}
