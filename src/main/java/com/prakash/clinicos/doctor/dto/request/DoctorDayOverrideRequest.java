package com.prakash.clinicos.doctor.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.prakash.clinicos.doctor.entity.OverrideType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request to set a date-specific override for a doctor.
 * If an override already exists for this date it is replaced (upsert).
 *
 * Cross-field rules (validated in service):
 *   DAY_OFF      → startTime and endTime must be null
 *   LATE_START   → startTime required (new effective start), endTime must be null
 *   EARLY_END    → endTime required (new effective end), startTime must be null
 *   CUSTOM_HOURS → both startTime and endTime required; endTime > startTime
 *
 * The "doctor arrives late AND leaves early" case → use CUSTOM_HOURS with both times.
 *
 * Time values must align to 10-minute boundaries so slot generation stays clean.
 */
@Getter
@Setter
public class DoctorDayOverrideRequest {

    @NotNull(message = "overrideDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate overrideDate;

    @NotNull(message = "overrideType is required (DAY_OFF, LATE_START, EARLY_END, CUSTOM_HOURS)")
    private OverrideType overrideType;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String reason;
}
