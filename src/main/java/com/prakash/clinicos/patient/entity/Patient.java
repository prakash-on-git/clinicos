package com.prakash.clinicos.patient.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clinic this patient belongs to. Plain FK — no cross-module entity reference. */
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    /**
     * Optional link to a users row. Allows the patient to log in and view their
     * own profile and appointments. Set via link-user endpoint; not required at registration.
     */
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /**
     * Primary contact number. Required and unique per clinic.
     * Receptionists look up patients by phone number constantly.
     */
    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY — stored as string, validated in service */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Gender gender;

    // ── Medical background ────────────────────────────────────────────────────

    /** e.g. A+, O-, AB+ */
    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    /** Free text: "Penicillin, Aspirin, Shellfish" */
    @Column(columnDefinition = "TEXT")
    private String allergies;

    /** Free text: "Diabetes Type 2, Hypertension" */
    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;

    /** Free text: "Metformin 500mg, Amlodipine 5mg" */
    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    // ── Emergency contact ─────────────────────────────────────────────────────

    @Column(name = "emergency_contact_name", length = 255)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    /** e.g. "Mother", "Spouse", "Guardian" */
    @Column(name = "emergency_contact_relation", length = 100)
    private String emergencyContactRelation;

    // ── Other ─────────────────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String address;

    /** Internal staff notes — not shown to the patient in self-service. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Inactive = the patient is not currently being seen (e.g. transferred elsewhere).
     * Staff can see inactive patients; they just won't appear in the default active list.
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

    /** ID of the staff member (user) who registered this patient. */
    @Column(name = "created_by")
    private Long createdBy;

    /** Patient has opted out of SMS notifications. */
    @Column(name = "sms_opt_out", nullable = false)
    @Builder.Default
    private boolean smsOptOut = false;

    /** Patient has opted out of email notifications. */
    @Column(name = "email_opt_out", nullable = false)
    @Builder.Default
    private boolean emailOptOut = false;
}
