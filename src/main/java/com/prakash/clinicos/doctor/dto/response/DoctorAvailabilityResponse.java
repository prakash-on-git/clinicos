package com.prakash.clinicos.doctor.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Result of the slot availability computation for a doctor on a specific date.
 *
 * When available=false, slots is empty and unavailableReason explains why.
 * When available=true, slots contains the "HH:mm" start time of each free 10-minute window.
 *
 * In Phase 4 all slots are free (no appointments booked yet).
 * Phase 6 (Appointments) will mark slots as BOOKED and subtract them from availableSlots.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorAvailabilityResponse {

    private Long doctorId;
    private String doctorName;
    private Long clinicId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private DayOfWeek dayOfWeek;

    /** false when the doctor has no slots for this date for any reason. */
    private boolean available;

    /**
     * Human-readable reason when available=false.
     * Values: DOCTOR_INACTIVE, DOCTOR_ON_LEAVE, CLINIC_CLOSED,
     *         NO_SCHEDULE_FOR_DAY, DAY_OVERRIDE_DAY_OFF
     */
    private String unavailableReason;

    /** Effective working windows after applying any override. */
    private List<WorkingWindow> workingWindows;

    /** Recurring breaks that apply to this day. */
    private List<BreakInfo> breaks;

    /**
     * 10-minute slot start times in "HH:mm" format.
     * Each entry represents a slot from T to T+10 minutes.
     * Slots are sorted chronologically.
     */
    private List<String> slots;

    /** Total number of available 10-minute slots. */
    private int totalSlots;

    // ── Nested types ──────────────────────────────────────────────────────────

    @Getter
    @Builder
    public static class WorkingWindow {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime effectiveStart;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime effectiveEnd;

        /** true if a DoctorDayOverride was applied to compute this window. */
        private boolean overrideApplied;

        /** The type of override applied: LATE_START, EARLY_END, CUSTOM_HOURS. null if none. */
        private String overrideType;
    }

    @Getter
    @Builder
    public static class BreakInfo {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime breakStart;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime breakEnd;

        private String breakType;
        private String label;
    }
}
