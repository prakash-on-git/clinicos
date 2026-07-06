package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.DoctorDayOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorDayOverrideRepository extends JpaRepository<DoctorDayOverride, Long> {

    Optional<DoctorDayOverride> findByDoctorIdAndOverrideDate(Long doctorId, LocalDate overrideDate);

    boolean existsByDoctorIdAndOverrideDate(Long doctorId, LocalDate overrideDate);

    /** Upcoming overrides from today onwards (for admin view and booking). */
    List<DoctorDayOverride> findByDoctorIdAndOverrideDateGreaterThanEqualOrderByOverrideDateAsc(
            Long doctorId, LocalDate from);

    @Transactional
    void deleteByDoctorIdAndOverrideDate(Long doctorId, LocalDate overrideDate);
}
