package com.prakash.clinicos.medical.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VitalsResponse {

    private Long id;
    private Long clinicId;
    private Long patientId;
    private Long appointmentId;

    private String patientName;

    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer pulseBpm;
    private BigDecimal temperatureCelsius;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private Integer spo2Percent;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
