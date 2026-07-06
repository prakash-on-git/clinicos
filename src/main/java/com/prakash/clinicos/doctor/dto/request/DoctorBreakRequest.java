package com.prakash.clinicos.doctor.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.prakash.clinicos.doctor.entity.BreakType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/**
 * One break window for a specific day.
 * Used in PUT /doctors/{id}/breaks/{day} — replaces all breaks for that day.
 *
 * Service validates:
 *   - breakEnd > breakStart
 *   - breakStart and breakEnd align to 10-minute boundaries (avoid fractional slots)
 *   - breaks do not overlap each other on the same day
 *   - breaks fall within the doctor's working window for that day
 */
@Getter
@Setter
public class DoctorBreakRequest {

    @NotNull(message = "breakStart is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime breakStart;

    @NotNull(message = "breakEnd is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime breakEnd;

    private BreakType breakType = BreakType.BREAK;

    /** Optional display name e.g. "Lunch Break", "Zuhr Prayer". */
    private String label;
}
