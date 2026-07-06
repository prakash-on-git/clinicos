package com.prakash.clinicos.audit.repository;

import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Filtered, paginated audit log search.
     * All filter params are optional — null means "no filter".
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.clinicId = :clinicId
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:action     IS NULL OR a.action     = :action)
              AND (:changedBy  IS NULL OR a.changedBy  = :changedBy)
              AND (:from       IS NULL OR a.createdAt >= :from)
              AND (:to         IS NULL OR a.createdAt <= :to)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(
            @Param("clinicId")   Long clinicId,
            @Param("entityType") String entityType,
            @Param("action")     AuditAction action,
            @Param("changedBy")  Long changedBy,
            @Param("from")       LocalDateTime from,
            @Param("to")         LocalDateTime to,
            Pageable pageable);
}
