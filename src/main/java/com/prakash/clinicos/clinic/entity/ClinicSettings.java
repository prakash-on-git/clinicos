package com.prakash.clinicos.clinic.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Appointment booking policy for a clinic.
 * One-to-one with Clinic. Auto-created with defaults when a clinic is created.
 *
 * Kept in a separate table (not on the clinics row) so the clinics table stays
 * focused on identity/location/status (single responsibility principle).
 */
@Entity
@Table(name = "clinic_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false, unique = true)
    private Clinic clinic;

    /** Duration of each appointment slot in minutes. Maps to slot grid in the booking UI. */
    @Column(name = "appointment_duration_mins", nullable = false)
    @Builder.Default
    private int appointmentDurationMins = 20;

    /** How many calendar days ahead a patient can book. Prevents overbooking far future slots. */
    @Column(name = "advance_booking_days", nullable = false)
    @Builder.Default
    private int advanceBookingDays = 30;

    /**
     * How many hours before the appointment a cancellation is allowed without penalty.
     * 0 = cancellation allowed any time (no window).
     * 24 = must cancel at least 24 hours before appointment.
     */
    @Column(name = "cancellation_window_hours", nullable = false)
    @Builder.Default
    private int cancellationWindowHours = 24;

    /** Maximum patients per day. 0 = unlimited. Applied on top of slot capacity. */
    @Column(name = "max_patients_per_day", nullable = false)
    @Builder.Default
    private int maxPatientsPerDay = 0;

    /** Whether walk-in patients (without an appointment) are accepted at this clinic. */
    @Column(name = "allow_walk_ins", nullable = false)
    @Builder.Default
    private boolean allowWalkIns = true;

    /**
     * TRUE  → booking is confirmed immediately on creation.
     * FALSE → booking stays PENDING until receptionist manually approves.
     * Useful for clinics that review bookings before confirming.
     */
    @Column(name = "auto_confirm_appointments", nullable = false)
    @Builder.Default
    private boolean autoConfirmAppointments = true;
}
