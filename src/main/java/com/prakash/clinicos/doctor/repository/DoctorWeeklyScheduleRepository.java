package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.DoctorWeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorWeeklyScheduleRepository extends JpaRepository<DoctorWeeklySchedule, Long> {

    List<DoctorWeeklySchedule> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    List<DoctorWeeklySchedule> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

    @Transactional
    void deleteByDoctorId(Long doctorId);

    @Transactional
    void deleteByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}
