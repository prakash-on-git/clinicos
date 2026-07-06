package com.prakash.clinicos.subscription.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.subscription.dto.response.PlanResponse;
import com.prakash.clinicos.subscription.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Plans", description = "List available subscription plans - FREE, PRO, ENTERPRISE. Public endpoint.")
@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final SubscriptionService subscriptionService;

    public PlanController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * GET /api/v1/plans
     * Public endpoint — returns all active subscription plans.
     */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> getPlans() {
        return ResponseEntity.ok(subscriptionService.getPlans());
    }
}
