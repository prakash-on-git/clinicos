package com.prakash.clinicos.subscription.dto.request;

import com.prakash.clinicos.subscription.entity.PlanTier;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SubscribeRequest {

    @NotNull
    private PlanTier tier;
}
