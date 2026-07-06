package com.prakash.clinicos.doctor.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreatmentTypeResponse {

    private Long id;
    private Long clinicId;
    private String name;
    private String description;
    private int defaultDurationMins;
    private BigDecimal defaultFee;
    private String colorHex;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
