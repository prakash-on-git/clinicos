package com.prakash.clinicos.doctor.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A treatment offered by a clinic (e.g. "General Consultation", "ECG", "Blood Pressure Check").
 *
 * Duration is always a multiple of 10 minutes so it maps cleanly onto 10-minute slots.
 * Individual doctors can override the duration and fee via DoctorTreatment.
 *
 * Soft-deleted treatments remain visible in historical appointment records.
 */
@Entity
@Table(name = "treatment_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to clinics table — stored as plain Long to avoid cross-module coupling. */
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Default slot duration in minutes. Must be >= 10 and a multiple of 10.
     * Individual doctors can override this via DoctorTreatment.customDurationMins.
     */
    @Column(name = "default_duration_mins", nullable = false)
    private int defaultDurationMins;

    @Column(name = "default_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal defaultFee = BigDecimal.ZERO;

    /** Optional UI hex color e.g. "#3B82F6". Lets front-end color-code appointment cards. */
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;
}
