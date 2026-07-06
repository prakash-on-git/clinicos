package com.prakash.clinicos.queue.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Request to check in a patient who has a booked appointment.
 * Automatically generates a queue token linked to the appointment.
 */
@Getter
public class CheckInRequest {

    @NotNull(message = "appointmentId is required")
    private Long appointmentId;

    /** Optional additional notes (supplements the appointment's reason field). */
    private String notes;
}
