package com.prakash.clinicos.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.prakash.clinicos.subscription.entity.PlanTier;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Builder
@JsonInclude(NON_NULL)
public class PlanResponse {

    private Long id;
    private PlanTier tier;
    private String displayName;
    private Integer maxDoctors;          // null shown as absent (unlimited)
    private Integer maxPatientsPerMonth;
    private BigDecimal priceMonthly;
}
