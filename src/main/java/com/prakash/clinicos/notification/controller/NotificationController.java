package com.prakash.clinicos.notification.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.prakash.clinicos.notification.dto.response.NotificationLogResponse;
import com.prakash.clinicos.notification.dto.response.NotificationPreferenceResponse;
import com.prakash.clinicos.notification.entity.NotificationChannel;
import com.prakash.clinicos.notification.entity.NotificationStatus;
import com.prakash.clinicos.notification.entity.NotificationType;
import com.prakash.clinicos.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Notifications", description = "View notification logs and manage per-clinic notification preferences.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── Audit log ─────────────────────────────────────────────────────────────

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<NotificationLogResponse>> getLogs(
            @PathVariable Long clinicId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                notificationService.getLogs(clinicId, type, channel, status, from, to, pageable));
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            @PathVariable Long clinicId) {
        return ResponseEntity.ok(notificationService.getPreferences(clinicId));
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @PathVariable Long clinicId,
            @RequestBody UpdateNotificationPreferencesRequest req) {
        return ResponseEntity.ok(notificationService.updatePreferences(clinicId, req));
    }
}
