package com.prakash.clinicos.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Builder
@JsonInclude(NON_NULL)
public class PlatformRevenueResponse {

    private long totalActiveClinics;
    private BigDecimal monthlyRecurringRevenue;   // sum of price_monthly for ACTIVE subs
    private Map<String, Long> activeClinicsByPlan; // tier -> count
    private Map<String, BigDecimal> revenueByPlan; // tier -> (count * price)
}
