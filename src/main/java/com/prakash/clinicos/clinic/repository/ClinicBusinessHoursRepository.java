package com.prakash.clinicos.clinic.repository;

import com.prakash.clinicos.clinic.entity.ClinicBusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

public interface ClinicBusinessHoursRepository extends JpaRepository<ClinicBusinessHours, Long> {

    /** All shifts across all days for a clinic. Used when building the full clinic response. */
    List<ClinicBusinessHours> findByClinicIdOrderByDayOfWeekAscOpenTimeAsc(Long clinicId);

    /** All shifts for a specific day. Used by isCurrentlyOpen computation. */
    List<ClinicBusinessHours> findByClinicIdAndDayOfWeek(Long clinicId, DayOfWeek dayOfWeek);

    /** Replaces all shifts for one day (bulk update for single day). */
    @Transactional
    void deleteByClinicIdAndDayOfWeek(Long clinicId, DayOfWeek dayOfWeek);

    /** Replaces entire week's schedule (bulk update for full week). */
    @Transactional
    void deleteByClinicId(Long clinicId);
}
