package com.prakash.clinicos.clinic.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A specific date on which the clinic is closed.
 * Examples: public holidays, annual staff training, doctor leave.
 *
 * This is for PLANNED future closures, not emergency closures.
 * Emergency closures use the isEmergencyClosed flag on the Clinic entity.
 *
 * LocalDate → PostgreSQL DATE. Timezone is implicit from the Clinic's timezone field.
 */
@Entity
@Table(name = "clinic_closure_dates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicClosureDate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    /** Date in the clinic's local timezone. Stored as DATE (no time component). */
    @Column(name = "closure_date", nullable = false)
    private LocalDate closureDate;

    /** Human-readable reason for the closure. Shown in UI and booking pages. */
    @Column(length = 255)
    private String reason;
}
