package com.prakash.clinicos.clinic.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clinics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clinic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identity ─────────────────────────────────────────────────────────────
    @Column(nullable = false, length = 255)
    private String name;

    /** URL-safe identifier. Auto-generated from name. Unique across all clinics. */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    // ── Contact ───────────────────────────────────────────────────────────────
    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 512)
    private String website;

    // ── Address ───────────────────────────────────────────────────────────────
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    @Builder.Default
    private String country = "India";

    /**
     * IANA timezone ID (e.g. "Asia/Kolkata", "America/New_York").
     * All business hours are stored as local clinic time and interpreted in this timezone.
     * IANA IDs handle daylight saving automatically; never store "+05:30" style offsets.
     */
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    // ── Ownership ─────────────────────────────────────────────────────────────
    /**
     * Stored as a plain Long FK instead of @ManyToOne User.
     * Reason: avoids cross-module coupling between the clinic and auth modules.
     * If you need the User object, inject UserRepository where needed.
     */
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    // ── Open mode ─────────────────────────────────────────────────────────────
    /**
     * When TRUE: clinic is always open; business hours rows are ignored.
     * isCurrentlyOpen() returns true (unless emergency closed).
     * The hours rows are preserved so the admin can revert to a schedule later.
     */
    @Column(name = "is_24_7", nullable = false)
    @Builder.Default
    private boolean alwaysOpen = false;

    // ── Emergency close ───────────────────────────────────────────────────────
    /**
     * Temporary suspension without deleting the clinic.
     * Use case: power outage, doctor emergency, walk-in flood.
     * Reversal: POST /api/v1/clinics/{id}/emergency-reopen.
     * Distinguishable from soft delete: this can be undone; delete cannot.
     */
    @Column(name = "is_emergency_closed", nullable = false)
    @Builder.Default
    private boolean emergencyClosed = false;

    @Column(name = "emergency_close_reason", length = 500)
    private String emergencyCloseReason;

    @Column(name = "emergency_closed_at")
    private LocalDateTime emergencyClosedAt;

    @Column(name = "emergency_closed_by")
    private Long emergencyClosedBy;

    // ── Soft delete ───────────────────────────────────────────────────────────
    /**
     * Permanent deactivation. Once deleted, cannot be restored via API.
     * Data is preserved for audit purposes (never hard-deleted from DB).
     * All queries in ClinicRepository filter on isDeleted = false.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    // ── Relationships ─────────────────────────────────────────────────────────
    /**
     * All lazy-loaded. The service fetches them explicitly when building the full response.
     * CascadeType.ALL + orphanRemoval: deleting a clinic deletes all its hours/closures/settings.
     */
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ClinicBusinessHours> businessHours = new ArrayList<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ClinicClosureDate> closureDates = new ArrayList<>();

    @OneToOne(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ClinicSettings settings;
}
