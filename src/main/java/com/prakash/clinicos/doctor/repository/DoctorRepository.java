package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByIdAndDeletedFalse(Long id);

    Optional<Doctor> findBySlugAndDeletedFalse(String slug);

    Optional<Doctor> findByUserIdAndDeletedFalse(Long userId);

    boolean existsBySlug(String slug);

    /** Prevent duplicate email within the same clinic. */
    boolean existsByClinicIdAndEmailAndDeletedFalse(Long clinicId, String email);

    /** Ensure a user account isn't already linked to another doctor. */
    boolean existsByUserIdAndDeletedFalse(Long userId);

    /** Paginated list of non-deleted doctors in a clinic. */
    Page<Doctor> findByClinicIdAndDeletedFalse(Long clinicId, Pageable pageable);

    /** Full list for reporting / cache building. */
    List<Doctor> findByClinicIdAndDeletedFalse(Long clinicId);

    /** Filter by active status as well (for booking-facing views). */
    Page<Doctor> findByClinicIdAndDeletedFalseAndActive(Long clinicId, boolean active, Pageable pageable);

    /** Count non-deleted doctors in a clinic (used for subscription plan enforcement). */
    long countByClinicIdAndDeletedFalse(Long clinicId);
}
