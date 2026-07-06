package com.prakash.clinicos.doctor.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A date-specific deviation from the doctor's regular weekly schedule.
 *
 * One row per (doctor, date). The override_type controls what start_time/end_time mean:
 *
 *  DAY_OFF      → doctor absent all day. No slots generated. start/end are null.
 *  LATE_START   → doctor arrives late. start_time = new effective start. end_time null.
 *  EARLY_END    → doctor leaves early. end_time = new effective end. start_time null.
 *  CUSTOM_HOURS → completely replaces weekly schedule for this date. Both times required.
 *                 Use for: arrive late AND leave early, or simply a different shift.
 */
@Entity
@Table(name = "doctor_day_overrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDayOverride extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "override_date", nullable = false)
    private LocalDate overrideDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "override_type", nullable = false, length = 20)
    private OverrideType overrideType;

    /** Effective start time. Used for LATE_START and CUSTOM_HOURS. */
    @Column(name = "start_time")
    private LocalTime startTime;

    /** Effective end time. Used for EARLY_END and CUSTOM_HOURS. */
    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_by")
    private Long createdBy;
}
