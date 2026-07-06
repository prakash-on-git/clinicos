package com.prakash.clinicos.doctor.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorTreatmentResponse {

    private Long id;
    private Long treatmentTypeId;
    private String treatmentName;
    private String colorHex;

    /** null if using the treatment type's default. */
    private Integer customDurationMins;

    /** null if using the treatment type's default. */
    private BigDecimal customFee;

    /** Resolved value: customDurationMins if set, otherwise treatmentType.defaultDurationMins. */
    private int effectiveDurationMins;

    /** Resolved value: customFee if set, otherwise treatmentType.defaultFee. */
    private BigDecimal effectiveFee;

    private boolean active;
}
