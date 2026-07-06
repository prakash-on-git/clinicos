package com.prakash.clinicos.audit.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.audit.dto.response.AuditLogResponse;
import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Audit Logs", description = "Query the immutable audit trail of all clinical data mutations.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getLogs(
            @PathVariable Long clinicId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Long changedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                auditService.getLogs(clinicId, entityType, action, changedBy, from, to, pageable));
    }
}
