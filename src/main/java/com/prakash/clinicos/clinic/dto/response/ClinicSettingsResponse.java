package com.prakash.clinicos.clinic.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClinicSettingsResponse {

    private int appointmentDurationMins;
    private int advanceBookingDays;
    private int cancellationWindowHours;
    private int maxPatientsPerDay;
    private boolean allowWalkIns;
    private boolean autoConfirmAppointments;
}
