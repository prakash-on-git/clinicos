package com.prakash.clinicos.clinic.repository;

import com.prakash.clinicos.clinic.entity.Clinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * All query methods explicitly filter deleted = false.
 *
 * Why not use @SQLRestriction("is_deleted = false")?
 * @SQLRestriction would also filter findById — which could silently hide
 * deleted clinics from SUPER_ADMIN who may need to audit them.
 * Explicit method names are clearer about what they include/exclude.
 */
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    Optional<Clinic> findByIdAndDeletedFalse(Long id);

    Optional<Clinic> findBySlugAndDeletedFalse(String slug);

    boolean existsBySlug(String slug);

    /** Used to prevent a CLINIC_ADMIN from creating a second clinic. */
    boolean existsByOwnerUserIdAndDeletedFalse(Long ownerUserId);

    /** SUPER_ADMIN: list all active clinics with pagination. */
    Page<Clinic> findAllByDeletedFalse(Pageable pageable);

    /** List active clinics in a specific city (for filtered view). */
    Page<Clinic> findAllByDeletedFalseAndCityIgnoreCase(String city, Pageable pageable);
}
