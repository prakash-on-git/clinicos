package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.DoctorBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorBreakRepository extends JpaRepository<DoctorBreak, Long> {

    List<DoctorBreak> findByDoctorIdOrderByDayOfWeekAscBreakStartAsc(Long doctorId);

    List<DoctorBreak> findByDoctorIdAndDayOfWeekOrderByBreakStartAsc(Long doctorId, DayOfWeek dayOfWeek);

    @Transactional
    void deleteByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

    @Transactional
    void deleteByDoctorId(Long doctorId);
}
