package com.prakash.clinicos.clinic.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * One row = one contiguous shift on one day of the week.
 *
 * Multi-shift example (morning + evening on Monday):
 *   Row 1: MONDAY, 09:00–13:00, "Morning"
 *   Row 2: MONDAY, 17:00–21:00, "Evening"
 *
 * Day off = no rows for that day.
 * 24/7 = alwaysOpen flag on Clinic entity; these rows are preserved but ignored.
 *
 * Times are stored in LOCAL clinic time (no timezone).
 * Timezone is on the Clinic entity — apply it during isCurrentlyOpen computation.
 */
@Entity
@Table(name = "clinic_business_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicBusinessHours extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    /**
     * java.time.DayOfWeek: MONDAY, TUESDAY, ..., SUNDAY.
     * @Enumerated(STRING) stores the enum name() — matches the SQL CHECK constraint.
     * DayOfWeek.MONDAY.getValue() = 1, SUNDAY = 7 (ISO-8601 week order).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /**
     * LocalTime → PostgreSQL TIME WITHOUT TIME ZONE.
     * No timezone info here — timezone lives on the Clinic row.
     * This is intentional: "09:00 AM clinic-local-time" is the correct semantic.
     */
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    /** Optional. Helps staff and UI distinguish shifts. E.g. "Morning", "Evening". */
    @Column(name = "shift_label", length = 50)
    private String shiftLabel;
}
