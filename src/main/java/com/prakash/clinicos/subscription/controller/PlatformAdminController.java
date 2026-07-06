package com.prakash.clinicos.subscription.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.subscription.dto.response.PlatformClinicResponse;
import com.prakash.clinicos.subscription.dto.response.PlatformRevenueResponse;
import com.prakash.clinicos.subscription.service.PlatformAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Platform Admin", description = "SUPER_ADMIN only - view all clinics with subscriptions and platform revenue.")
@RestController
@RequestMapping("/api/v1/admin")
public class PlatformAdminController {

    private final PlatformAdminService platformAdminService;

    public PlatformAdminController(PlatformAdminService platformAdminService) {
        this.platformAdminService = platformAdminService;
    }

    /**
     * GET /api/v1/admin/clinics
     * SUPER_ADMIN: paginated list of all clinics with subscription info.
     */
    @GetMapping("/clinics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<PlatformClinicResponse>> getAllClinics(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(platformAdminService.getAllClinics(pageable));
    }

    /**
     * GET /api/v1/admin/revenue
     * SUPER_ADMIN: platform-level revenue dashboard.
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PlatformRevenueResponse> getPlatformRevenue() {
        return ResponseEntity.ok(platformAdminService.getPlatformRevenue());
    }
}
