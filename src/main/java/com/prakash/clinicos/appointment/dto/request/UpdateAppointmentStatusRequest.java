package com.prakash.clinicos.appointment.dto.request;

import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Used to advance an appointment through its lifecycle.
 *
 * Allowed target statuses via this endpoint:
 *   CONFIRMED    – receptionist confirms a PENDING appointment
 *   IN_PROGRESS  – doctor has started seeing the patient (was CONFIRMED)
 *   COMPLETED    – consultation finished (was IN_PROGRESS or CONFIRMED)
 *   NO_SHOW      – patient didn't arrive (was CONFIRMED)
 *
 * CANCELLED uses its own endpoint (with optional cancellation reason).
 * RESCHEDULED is set automatically by the reschedule endpoint.
 * PENDING is the initial status and cannot be set manually.
 */
@Getter
public class UpdateAppointmentStatusRequest {

    @NotNull(message = "status is required")
    private AppointmentStatus status;

    /** Optional notes to add/update when changing status. */
    private String notes;
}
