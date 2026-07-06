package com.prakash.clinicos.doctor.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Junction between a Doctor and a TreatmentType — with optional per-doctor overrides.
 *
 * Effective duration = customDurationMins != null ? customDurationMins : treatmentType.defaultDurationMins
 * Effective fee      = customFee != null ? customFee : treatmentType.defaultFee
 *
 * This lets different doctors charge different fees and take different durations
 * for the same treatment type (e.g. a senior specialist may charge more and take longer).
 */
@Entity
@Table(name = "doctor_treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorTreatment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_type_id", nullable = false)
    private TreatmentType treatmentType;

    /**
     * If set, overrides treatmentType.defaultDurationMins for this doctor.
     * Must be >= 10 and a multiple of 10.
     */
    @Column(name = "custom_duration_mins")
    private Integer customDurationMins;

    /**
     * If set, overrides treatmentType.defaultFee for this doctor.
     */
    @Column(name = "custom_fee", precision = 10, scale = 2)
    private BigDecimal customFee;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Compute the effective duration for slot booking. */
    @Transient
    public int getEffectiveDurationMins() {
        return customDurationMins != null ? customDurationMins : treatmentType.getDefaultDurationMins();
    }

    /** Compute the effective fee for invoicing. */
    @Transient
    public BigDecimal getEffectiveFee() {
        return customFee != null ? customFee : treatmentType.getDefaultFee();
    }
}
