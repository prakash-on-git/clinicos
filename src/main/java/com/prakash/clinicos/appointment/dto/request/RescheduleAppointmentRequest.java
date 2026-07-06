package com.prakash.clinicos.appointment.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class RescheduleAppointmentRequest {

    @NotNull(message = "newDate is required")
    @FutureOrPresent(message = "Cannot reschedule to a past date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newDate;

    @NotNull(message = "newStartTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime newStartTime;

    /** Optional: reason for rescheduling (e.g. "Doctor unavailable", "Patient request"). */
    private String reason;
}
