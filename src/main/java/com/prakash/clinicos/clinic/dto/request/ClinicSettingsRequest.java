package com.prakash.clinicos.clinic.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicSettingsRequest {

    @NotNull
    @Min(value = 5, message = "Appointment duration must be at least 5 minutes")
    @Max(value = 240, message = "Appointment duration cannot exceed 240 minutes (4 hours)")
    private Integer appointmentDurationMins;

    @NotNull
    @Min(value = 1, message = "Advance booking must be at least 1 day")
    @Max(value = 365, message = "Advance booking cannot exceed 365 days")
    private Integer advanceBookingDays;

    @NotNull
    @Min(value = 0, message = "Cancellation window cannot be negative")
    @Max(value = 168, message = "Cancellation window cannot exceed 168 hours (1 week)")
    private Integer cancellationWindowHours;

    @NotNull
    @Min(value = 0, message = "Max patients cannot be negative (0 = unlimited)")
    private Integer maxPatientsPerDay;

    @NotNull
    private Boolean allowWalkIns;

    @NotNull
    private Boolean autoConfirmAppointments;
}
