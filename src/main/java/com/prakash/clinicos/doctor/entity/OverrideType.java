package com.prakash.clinicos.doctor.entity;

public enum OverrideType {
    /** Doctor is absent the entire day — no slots generated. */
    DAY_OFF,

    /** Doctor starts later than scheduled. Only start_time is set; end_time is null. */
    LATE_START,

    /** Doctor leaves earlier than scheduled. Only end_time is set; start_time is null. */
    EARLY_END,

    /**
     * Doctor works completely different hours this day.
     * Both start_time and end_time are set; the weekly schedule is ignored entirely.
     * Use when the doctor arrives late AND leaves early on the same day.
     */
    CUSTOM_HOURS
}
