package com.prakash.clinicos.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Immutable audit record — no updated_at, no soft-delete, never modified after insert.
 *
 * before_state / after_state are JSON snapshots of the entity response DTO.
 * Null before_state = CREATE.  Null after_state = DELETE.
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The clinic this event belongs to. Null for platform-level actions (e.g., SUPER_ADMIN ops). */
    @Column(name = "clinic_id")
    private Long clinicId;

    /** Entity class name in uppercase, e.g. "PATIENT", "DOCTOR", "APPOINTMENT". */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    /** The user who triggered the change. Null for scheduled/system actions. */
    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
