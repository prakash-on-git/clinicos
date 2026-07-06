package com.prakash.clinicos.medical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class PrescriptionMedicineRequest {

    @NotBlank(message = "medicineName is required")
    private String medicineName;

    @NotBlank(message = "dosage is required")
    private String dosage;

    @NotBlank(message = "frequency is required")
    private String frequency;

    @Positive(message = "durationDays must be positive")
    private Integer durationDays;

    private String route;
    private String notes;

    public String getMedicineName()  { return medicineName; }
    public String getDosage()        { return dosage; }
    public String getFrequency()     { return frequency; }
    public Integer getDurationDays() { return durationDays; }
    public String getRoute()         { return route; }
    public String getNotes()         { return notes; }
}
