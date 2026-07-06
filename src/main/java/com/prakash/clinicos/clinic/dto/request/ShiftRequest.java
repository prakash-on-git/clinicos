package com.prakash.clinicos.clinic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/**
 * A single shift block (open time → close time) for a specific day.
 * Used in PUT /clinics/{id}/hours/{day} — dayOfWeek comes from the path variable.
 *
 * Examples:
 *   { "openTime": "09:00", "closeTime": "13:00", "shiftLabel": "Morning" }
 *   { "openTime": "17:00", "closeTime": "21:00", "shiftLabel": "Evening" }
 */
@Getter
@Setter
public class ShiftRequest {

    @NotNull(message = "openTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "closeTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    /** Optional. E.g. "Morning", "Evening", "Night", "Lunch Session". */
    private String shiftLabel;
}
