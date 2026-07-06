package com.prakash.clinicos.clinic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A single shift with its day of week.
 * Used in PUT /clinics/{id}/hours (bulk full-week update).
 *
 * Send one entry per shift, multiple entries per day for multi-shift days:
 * [
 *   { "dayOfWeek": "MONDAY",    "openTime": "09:00", "closeTime": "13:00", "shiftLabel": "Morning" },
 *   { "dayOfWeek": "MONDAY",    "openTime": "17:00", "closeTime": "21:00", "shiftLabel": "Evening" },
 *   { "dayOfWeek": "TUESDAY",   "openTime": "09:00", "closeTime": "18:00" },
 *   { "dayOfWeek": "WEDNESDAY", "openTime": "09:00", "closeTime": "18:00" }
 * ]
 *
 * Days not mentioned = closed (no shifts). To close a day explicitly, just exclude it.
 */
@Getter
@Setter
public class DayShiftRequest {

    @NotNull(message = "dayOfWeek is required (MONDAY, TUESDAY, ..., SUNDAY)")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "openTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "closeTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    private String shiftLabel;
}
