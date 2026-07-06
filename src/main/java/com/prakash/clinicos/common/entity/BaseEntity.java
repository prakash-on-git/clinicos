package com.prakash.clinicos.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Inherited by every entity. Automatically populates created_at and updated_at
 * via JPA Auditing (@EnableJpaAuditing in JpaConfig).
 *
 * @MappedSuperclass  – JPA maps the fields into each child table.
 *                      No separate base_entity table is created.
 * @EntityListeners   – wires AuditingEntityListener to populate the timestamps.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
