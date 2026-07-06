package com.prakash.clinicos.appointment.entity;

/**
 * Lifecycle states of an appointment.
 *
 * Valid transitions:
 *   PENDING    → CONFIRMED  (receptionist confirms)
 *   PENDING    → CANCELLED  (cancel endpoint)
 *   PENDING    → RESCHEDULED (reschedule endpoint)
 *   CONFIRMED  → IN_PROGRESS (doctor starts the consultation)
 *   CONFIRMED  → CANCELLED  (cancel endpoint)
 *   CONFIRMED  → NO_SHOW    (patient didn't arrive)
 *   CONFIRMED  → RESCHEDULED (reschedule endpoint)
 *   IN_PROGRESS → COMPLETED  (consultation finished)
 *   COMPLETED, CANCELLED, NO_SHOW, RESCHEDULED — terminal states (no further transitions)
 */
public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED
}
