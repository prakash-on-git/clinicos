package com.prakash.clinicos.clinic.repository;

import com.prakash.clinicos.clinic.entity.ClinicClosureDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClinicClosureDateRepository extends JpaRepository<ClinicClosureDate, Long> {

    /** Used by isCurrentlyOpen — check if today is a closure date. */
    boolean existsByClinicIdAndClosureDate(Long clinicId, LocalDate date);

    /** All closure dates for a clinic (admin view). */
    List<ClinicClosureDate> findByClinicIdOrderByClosureDateAsc(Long clinicId);

    /** Future + today closure dates (for booking systems to check). */
    List<ClinicClosureDate> findByClinicIdAndClosureDateGreaterThanEqualOrderByClosureDateAsc(
            Long clinicId, LocalDate from);

    /** Verify ownership before allowing delete. */
    Optional<ClinicClosureDate> findByIdAndClinicId(Long id, Long clinicId);

    @Transactional
    void deleteByClinicId(Long clinicId);
}
