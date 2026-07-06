package com.prakash.clinicos.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.prakash.clinicos.subscription.entity.PlanTier;
import com.prakash.clinicos.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Builder
@JsonInclude(NON_NULL)
public class PlatformClinicResponse {

    private Long id;
    private String name;
    private String slug;
    private String city;
    private String email;
    private boolean deleted;
    private PlanTier planTier;
    private SubscriptionStatus subscriptionStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subscribedAt;
}
