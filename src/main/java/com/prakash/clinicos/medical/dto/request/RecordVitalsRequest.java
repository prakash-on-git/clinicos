package com.prakash.clinicos.medical.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * All fields are optional — record whatever is measured.
 * At least one vital sign should be provided (validated in service).
 */
public class RecordVitalsRequest {

    @Min(value = 50, message = "systolicBp seems too low (min 50)")
    @Max(value = 300, message = "systolicBp seems too high (max 300)")
    private Integer systolicBp;

    @Min(value = 30, message = "diastolicBp seems too low (min 30)")
    @Max(value = 200, message = "diastolicBp seems too high (max 200)")
    private Integer diastolicBp;

    @Min(value = 20, message = "pulseBpm seems too low (min 20)")
    @Max(value = 300, message = "pulseBpm seems too high (max 300)")
    private Integer pulseBpm;

    private BigDecimal temperatureCelsius;

    @Positive(message = "weightKg must be positive")
    private BigDecimal weightKg;

    @Positive(message = "heightCm must be positive")
    private BigDecimal heightCm;

    @Min(value = 0, message = "spo2Percent min 0")
    @Max(value = 100, message = "spo2Percent max 100")
    private Integer spo2Percent;

    private String notes;

    public Integer getSystolicBp()              { return systolicBp; }
    public Integer getDiastolicBp()             { return diastolicBp; }
    public Integer getPulseBpm()                { return pulseBpm; }
    public BigDecimal getTemperatureCelsius()   { return temperatureCelsius; }
    public BigDecimal getWeightKg()             { return weightKg; }
    public BigDecimal getHeightCm()             { return heightCm; }
    public Integer getSpo2Percent()             { return spo2Percent; }
    public String getNotes()                    { return notes; }
}
