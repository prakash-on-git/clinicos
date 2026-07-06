package com.prakash.clinicos.doctor.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    /** Nullable — linked later when the doctor creates a user account. */
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    /** URL-safe identifier, auto-generated from fullName. */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    /** e.g. "MBBS, MD (Cardiology)" */
    @Column(length = 255)
    private String qualification;

    /** e.g. "Cardiologist", "General Physician" */
    @Column(length = 255)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    /** Medical council registration number. */
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    /** Default fee for a consultation (can be overridden per treatment type). */
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    /**
     * Temporarily inactive = not taking new appointments.
     * Does NOT mean deleted. Receptionist/admin can see an inactive doctor;
     * patients cannot book them. Useful for: onboarding, sabbatical, parental leave.
     */
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
