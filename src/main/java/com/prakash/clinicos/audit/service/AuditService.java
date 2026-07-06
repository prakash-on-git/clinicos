package com.prakash.clinicos.audit.service;

import tools.jackson.databind.ObjectMapper;
import com.prakash.clinicos.audit.dto.response.AuditLogResponse;
import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.entity.AuditLog;
import com.prakash.clinicos.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Centralized audit logging service.
 *
 * Callers pass the entity type, ID, action, and before/after state snapshots.
 * Snapshots can be any object — they are serialized to JSON via Jackson.
 *
 * Audit failures NEVER propagate to callers. If serialization or DB insert fails,
 * the exception is caught and logged at ERROR level, but the calling operation
 * is not affected.
 *
 * IP address is extracted from the active HTTP request via RequestContextHolder
 * (works in servlet threads; returns null for scheduled/async contexts).
 */
@Service
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Writes one audit log entry. Never throws — all exceptions are swallowed.
     *
     * @param clinicId   The clinic scope (null for platform-level actions)
     * @param entityType Uppercase entity name, e.g. "PATIENT", "DOCTOR"
     * @param entityId   Primary key of the affected entity
     * @param action     CREATE / UPDATE / DELETE
     * @param before     State before the change (null for CREATE)
     * @param after      State after the change (null for DELETE)
     * @param changedBy  User ID of the actor
     */
    public void log(Long clinicId, String entityType, Long entityId,
                    AuditAction action, Object before, Object after, Long changedBy) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .clinicId(clinicId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .changedBy(changedBy)
                    .beforeState(serialize(before))
                    .afterState(serialize(after))
                    .ipAddress(resolveIp())
                    .build());
        } catch (Exception ex) {
            log.error("Failed to write audit log [{} {} id={}]: {}",
                    action, entityType, entityId, ex.getMessage());
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogs(Long clinicId,
                                           String entityType,
                                           AuditAction action,
                                           Long changedBy,
                                           LocalDateTime from,
                                           LocalDateTime to,
                                           Pageable pageable) {
        return auditLogRepository
                .search(clinicId, entityType, action, changedBy, from, to, pageable)
                .map(this::toResponse);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String serialize(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            log.warn("Audit snapshot serialization failed: {}", ex.getMessage());
            return "\"" + obj + "\"";  // fallback: store toString as a JSON string
        }
    }

    /**
     * Extracts the client IP from the active servlet request.
     * Honours X-Forwarded-For for requests behind a reverse proxy.
     * Returns null when called outside an HTTP thread (scheduler, tests).
     */
    private String resolveIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return forwarded != null
                    ? forwarded.split(",")[0].trim()
                    : request.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return AuditLogResponse.builder()
                .id(entry.getId())
                .clinicId(entry.getClinicId())
                .entityType(entry.getEntityType())
                .entityId(entry.getEntityId())
                .action(entry.getAction())
                .changedBy(entry.getChangedBy())
                .beforeState(entry.getBeforeState())
                .afterState(entry.getAfterState())
                .ipAddress(entry.getIpAddress())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
