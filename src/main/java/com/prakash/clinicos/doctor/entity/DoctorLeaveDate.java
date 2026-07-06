package com.prakash.clinicos.doctor.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A planned/HR-tracked full-day absence for a doctor.
 * One row = one calendar date. Multi-day leave = multiple rows.
 *
 * Different from DoctorDayOverride (DAY_OFF):
 *   - Leave = planned in advance, multi-day, HR context.
 *   - DAY_OFF override = operational, same-day decision, no HR tracking.
 *
 * The availability algorithm checks both: if either is set for a date,
 * the doctor has no slots that day.
 */
@Entity
@Table(name = "doctor_leave_dates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorLeaveDate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    @Builder.Default
    private LeaveType leaveType = LeaveType.OTHER;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_by")
    private Long createdBy;
}
