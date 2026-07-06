package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.DoctorLeaveDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorLeaveDateRepository extends JpaRepository<DoctorLeaveDate, Long> {

    boolean existsByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);

    Optional<DoctorLeaveDate> findByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);

    /** Upcoming leave (today onwards) — used by availability checker and patient-facing views. */
    List<DoctorLeaveDate> findByDoctorIdAndLeaveDateGreaterThanEqualOrderByLeaveDateAsc(
            Long doctorId, LocalDate from);

    /** All leave dates (admin history view). */
    List<DoctorLeaveDate> findByDoctorIdOrderByLeaveDateDesc(Long doctorId);

    @Transactional
    void deleteByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);
}
