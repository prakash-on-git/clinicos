package com.prakash.clinicos.queue.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.queue.dto.request.CheckInRequest;
import com.prakash.clinicos.queue.dto.request.GenerateTokenRequest;
import com.prakash.clinicos.queue.dto.response.QueueTokenResponse;
import com.prakash.clinicos.queue.service.QueueService;
import com.prakash.clinicos.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the Queue Management module.
 *
 * Base path: /api/v1/clinics/{clinicId}/queue
 *
 * All endpoints require authentication.
 * Write operations require CLINIC_ADMIN or SUPER_ADMIN.
 * Read operations (today's queue, current, waiting) require any valid JWT.
 */
@Tag(name = "Queue", description = "Walk-in queue management - generate tokens, check in, call, start, and complete.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Token generation
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/clinics/{clinicId}/queue/tokens
     *
     * Generates a walk-in token for a patient who has no appointment.
     * Token number is sequential per doctor per day (resets to 1 each morning).
     */
    @PostMapping("/tokens")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> generateToken(
            @PathVariable Long clinicId,
            @Valid @RequestBody GenerateTokenRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queueService.generateToken(clinicId, request, principal));
    }

    /**
     * POST /api/v1/clinics/{clinicId}/queue/checkin
     *
     * Checks in a patient who has a booked appointment.
     * Auto-generates a queue token linked to the appointment.
     * Also auto-confirms the appointment (PENDING → CONFIRMED) if not already confirmed.
     */
    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> checkIn(
            @PathVariable Long clinicId,
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queueService.checkIn(clinicId, request, principal));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Read queue
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/clinics/{clinicId}/queue/today?doctorId=
     *
     * Today's full queue for the clinic (all statuses, all doctors).
     * Pass ?doctorId= to filter to a specific doctor's queue.
     * Ordered by doctor then by token number.
     *
     * Includes tokensAhead and estimatedWaitMins for WAITING tokens.
     */
    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QueueTokenResponse>> getTodayQueue(
            @PathVariable Long clinicId,
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(queueService.getTodayQueue(clinicId, doctorId));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/queue/waiting?doctorId=
     *
     * Only WAITING tokens — the "who is next" view for the receptionist screen.
     */
    @GetMapping("/waiting")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QueueTokenResponse>> getWaiting(
            @PathVariable Long clinicId,
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(queueService.getWaiting(clinicId, doctorId));
    }

    /**
     * GET /api/v1/clinics/{clinicId}/queue/current?doctorId=
     *
     * Only IN_PROGRESS tokens — who is currently being seen by a doctor.
     * A multi-doctor clinic will typically have one IN_PROGRESS per doctor.
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QueueTokenResponse>> getCurrent(
            @PathVariable Long clinicId,
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(queueService.getCurrent(clinicId, doctorId));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Status transitions
    // ════════════════════════════════════════════════════════════════════════

    /**
     * PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/call
     *
     * WAITING → CALLED.
     * Announce "Token #N, please proceed to Dr. X's room."
     * Records calledAt and calledBy.
     */
    @PatchMapping("/{tokenId}/call")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> callToken(
            @PathVariable Long clinicId,
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(queueService.callToken(clinicId, tokenId, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/start
     *
     * CALLED → IN_PROGRESS.
     * Doctor has started the consultation.
     * Records startedAt and mirrors status on the linked appointment (if any).
     */
    @PatchMapping("/{tokenId}/start")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> startConsultation(
            @PathVariable Long clinicId,
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(queueService.startConsultation(clinicId, tokenId, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/complete
     *
     * IN_PROGRESS → COMPLETED.
     * Consultation finished. Records completedAt.
     * Also marks the linked appointment COMPLETED (if any).
     */
    @PatchMapping("/{tokenId}/complete")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> completeToken(
            @PathVariable Long clinicId,
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(queueService.completeToken(clinicId, tokenId, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/skip
     *
     * CALLED → SKIPPED.
     * Patient didn't respond when called. Frees the slot for the next patient.
     * Use /recall if the patient returns.
     */
    @PatchMapping("/{tokenId}/skip")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> skipToken(
            @PathVariable Long clinicId,
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(queueService.skipToken(clinicId, tokenId, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/recall
     *
     * SKIPPED → WAITING (with a new token number at the end of the queue).
     * Patient has returned. They re-enter at the end — fair to everyone who waited.
     */
    @PatchMapping("/{tokenId}/recall")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> recallToken(
            @PathVariable Long clinicId,
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(queueService.recallToken(clinicId, tokenId, principal));
    }

    /**
     * PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/cancel
     *
     * Any non-terminal state → CANCELLED.
     * Patient left the clinic without being seen.
     */
    @PatchMapping("/{tokenId}/cancel")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueTokenResponse> cancelToken(
            @PathVariable Long clinicId,
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(queueService.cancelToken(clinicId, tokenId, principal));
    }
}
