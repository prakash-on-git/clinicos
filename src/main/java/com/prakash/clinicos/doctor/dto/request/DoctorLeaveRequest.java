package com.prakash.clinicos.doctor.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.prakash.clinicos.doctor.entity.LeaveType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Request to mark one or more leave dates for a doctor.
 * Sending multiple dates in one call lets an admin book a multi-day vacation at once
 * without making N separate API calls.
 *
 * Service validates:
 *   - No duplicate entries for the same date (409 Conflict)
 *   - Dates are not in the past (configurable; currently a warning, not an error)
 */
@Getter
@Setter
public class DoctorLeaveRequest {

    @NotEmpty(message = "At least one leave date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<@NotNull LocalDate> leaveDates;

    @NotNull(message = "leaveType is required (SICK, VACATION, CONFERENCE, EMERGENCY, PERSONAL, OTHER)")
    private LeaveType leaveType;

    private String reason;
}
