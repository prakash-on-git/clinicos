package com.prakash.clinicos.queue.entity;

/**
 * Lifecycle of a queue token.
 *
 * Valid transitions:
 *   WAITING    → CALLED      (receptionist/doctor calls the patient)
 *   CALLED     → IN_PROGRESS (doctor starts the consultation)
 *   CALLED     → SKIPPED     (patient didn't respond when called)
 *   IN_PROGRESS → COMPLETED  (consultation finished)
 *   SKIPPED    → WAITING     (patient came back; re-added at end of queue via /recall)
 *   WAITING / CALLED / SKIPPED → CANCELLED (patient left the clinic)
 */
public enum QueueStatus {
    WAITING,
    CALLED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED,
    CANCELLED
}
