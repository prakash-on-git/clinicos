package com.prakash.clinicos.queue.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /**
     * Set when patient checks in for a booked appointment.
     * Null for walk-ins with no prior appointment.
     */
    @Column(name = "appointment_id", unique = true)
    private Long appointmentId;

    /**
     * Sequential per (clinic, doctor, date). Starts at 1 each day.
     * Used for announcing: "Now serving Token #7 at Dr. Khan's room."
     */
    @Column(name = "token_number", nullable = false)
    private int tokenNumber;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;

    /** Receptionist notes at check-in, e.g. "Fever and cough". */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** When the patient was called to the doctor's room. */
    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "called_by")
    private Long calledBy;

    /** When the doctor started seeing this patient. */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /** When the consultation finished. */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Receptionist who generated this token. */
    @Column(name = "created_by")
    private Long createdBy;
}
