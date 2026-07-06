package com.prakash.clinicos.appointment.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain FKs — no cross-module @ManyToOne (same pattern as Doctor.clinicId)
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** Null = no specific treatment, slot duration defaults to 10 minutes. */
    @Column(name = "treatment_type_id")
    private Long treatmentTypeId;

    // ── Timing ────────────────────────────────────────────────────────────────

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** startTime + durationMins */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Duration snapshot at booking time.
     * Treatment default durations may change; existing appointments must not be affected.
     */
    @Column(name = "duration_mins", nullable = false)
    private int durationMins;

    // ── Status ────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /** Patient's stated reason for the visit / chief complaint. */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /** Internal notes added by doctor or staff during/after the consultation. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Cancellation ──────────────────────────────────────────────────────────

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // ── Confirmation ──────────────────────────────────────────────────────────

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // ── Reschedule chain ──────────────────────────────────────────────────────

    /**
     * When a patient reschedules, a new Appointment is created and this field
     * is set to the previous appointment's ID. Allows tracing the full history:
     * originalAppt → rescheduledAppt → rescheduledAgainAppt…
     */
    @Column(name = "rescheduled_from_id")
    private Long rescheduledFromId;

    // ── Audit ─────────────────────────────────────────────────────────────────

    /** User who booked (receptionist, patient via self-service, or admin). */
    @Column(name = "booked_by")
    private Long bookedBy;
}
