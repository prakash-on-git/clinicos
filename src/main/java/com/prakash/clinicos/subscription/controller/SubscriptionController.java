package com.prakash.clinicos.subscription.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.security.UserPrincipal;
import com.prakash.clinicos.subscription.dto.request.SubscribeRequest;
import com.prakash.clinicos.subscription.dto.response.SubscriptionResponse;
import com.prakash.clinicos.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscriptions", description = "View and upgrade clinic subscription plans.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * GET /api/v1/clinics/{clinicId}/subscription
     * Returns the current subscription for a clinic. Auto-assigns FREE if none exists.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long clinicId) {
        return ResponseEntity.ok(subscriptionService.getSubscription(clinicId));
    }

    /**
     * POST /api/v1/clinics/{clinicId}/subscription
     * Subscribe or upgrade the clinic to a plan.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @PathVariable Long clinicId,
            @Valid @RequestBody SubscribeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(subscriptionService.subscribe(clinicId, req, principal));
    }
}
