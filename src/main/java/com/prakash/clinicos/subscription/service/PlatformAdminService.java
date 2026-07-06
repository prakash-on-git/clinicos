package com.prakash.clinicos.subscription.service;

import com.prakash.clinicos.clinic.entity.Clinic;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.subscription.dto.response.PlatformClinicResponse;
import com.prakash.clinicos.subscription.dto.response.PlatformRevenueResponse;
import com.prakash.clinicos.subscription.entity.ClinicSubscription;
import com.prakash.clinicos.subscription.entity.Plan;
import com.prakash.clinicos.subscription.repository.ClinicSubscriptionRepository;
import com.prakash.clinicos.subscription.repository.PlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlatformAdminService {

    private final ClinicRepository clinicRepository;
    private final ClinicSubscriptionRepository clinicSubscriptionRepository;
    private final PlanRepository planRepository;

    public PlatformAdminService(ClinicRepository clinicRepository,
                                ClinicSubscriptionRepository clinicSubscriptionRepository,
                                PlanRepository planRepository) {
        this.clinicRepository = clinicRepository;
        this.clinicSubscriptionRepository = clinicSubscriptionRepository;
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public Page<PlatformClinicResponse> getAllClinics(Pageable pageable) {
        Page<Clinic> clinicsPage = clinicRepository.findAll(pageable);

        List<Long> clinicIds = clinicsPage.getContent()
                .stream()
                .map(Clinic::getId)
                .toList();

        // Batch load subscriptions to avoid N+1 queries
        Map<Long, ClinicSubscription> subsByClinicId = clinicSubscriptionRepository
                .findByClinicIdIn(clinicIds)
                .stream()
                .collect(Collectors.toMap(ClinicSubscription::getClinicId, s -> s));

        // Build plan id -> Plan map for subscriptions found
        List<Long> planIds = subsByClinicId.values()
                .stream()
                .map(ClinicSubscription::getPlanId)
                .distinct()
                .toList();

        Map<Long, Plan> plansById = planRepository.findAllById(planIds)
                .stream()
                .collect(Collectors.toMap(Plan::getId, p -> p));

        return clinicsPage.map(clinic -> {
            ClinicSubscription sub = subsByClinicId.get(clinic.getId());
            Plan plan = sub != null ? plansById.get(sub.getPlanId()) : null;

            return PlatformClinicResponse.builder()
                    .id(clinic.getId())
                    .name(clinic.getName())
                    .slug(clinic.getSlug())
                    .city(clinic.getCity())
                    .email(clinic.getEmail())
                    .deleted(clinic.isDeleted())
                    .planTier(plan != null ? plan.getTier() : null)
                    .subscriptionStatus(sub != null ? sub.getStatus() : null)
                    .subscribedAt(sub != null ? sub.getStartedAt() : null)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public PlatformRevenueResponse getPlatformRevenue() {
        // Build plan id -> Plan map
        Map<Long, Plan> plansById = planRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Plan::getId, p -> p));

        // Get active subscription counts per plan
        List<Object[]> rows = clinicSubscriptionRepository.countActiveByPlan();

        Map<String, Long> activeClinicsByPlan = new HashMap<>();
        Map<String, BigDecimal> revenueByPlan = new HashMap<>();
        BigDecimal totalMrr = BigDecimal.ZERO;
        long totalActiveClinics = 0;

        for (Object[] row : rows) {
            Long planId = (Long) row[0];
            long count = (Long) row[1];

            Plan plan = plansById.get(planId);
            if (plan == null) continue;

            String tierName = plan.getTier().name();
            activeClinicsByPlan.put(tierName, count);

            BigDecimal planRevenue = plan.getPriceMonthly()
                    .multiply(BigDecimal.valueOf(count));
            revenueByPlan.put(tierName, planRevenue);

            totalMrr = totalMrr.add(planRevenue);
            totalActiveClinics += count;
        }

        return PlatformRevenueResponse.builder()
                .totalActiveClinics(totalActiveClinics)
                .monthlyRecurringRevenue(totalMrr)
                .activeClinicsByPlan(activeClinicsByPlan)
                .revenueByPlan(revenueByPlan)
                .build();
    }
}
