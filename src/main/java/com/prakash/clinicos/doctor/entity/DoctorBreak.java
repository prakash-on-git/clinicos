package com.prakash.clinicos.doctor.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Recurring intra-day break for a doctor on a specific day of the week.
 * Examples: lunch 13:00–14:00 every Mon–Fri, prayer 13:20–13:50 daily.
 *
 * These slots are excluded during availability generation.
 *
 * Break times must align to 10-minute boundaries (e.g. 13:00, 13:10, 13:20)
 * to avoid creating fractional slots. Enforced in service layer.
 */
@Entity
@Table(name = "doctor_breaks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorBreak extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "break_start", nullable = false)
    private LocalTime breakStart;

    @Column(name = "break_end", nullable = false)
    private LocalTime breakEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "break_type", nullable = false, length = 20)
    @Builder.Default
    private BreakType breakType = BreakType.BREAK;

    /** Optional display label e.g. "Lunch Break", "Zuhr Prayer". */
    @Column(length = 100)
    private String label;
}
