package com.prakash.clinicos.doctor.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * One working shift for one day of the week.
 * Multiple shifts on the same day = multiple entries with the same dayOfWeek.
 *
 * Service validates:
 *   - endTime > startTime
 *   - no overlapping shifts on the same day
 *   - max 4 shifts per day
 *   - startTime and endTime align to 10-minute boundaries
 */
@Getter
@Setter
public class DayScheduleRequest {

    @NotNull(message = "dayOfWeek is required (e.g. MONDAY, TUESDAY)")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "startTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /** Optional label: "Morning OPD", "Evening OPD". */
    private String shiftLabel;
}
