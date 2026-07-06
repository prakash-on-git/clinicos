package com.prakash.clinicos.patient.repository;

import com.prakash.clinicos.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByIdAndDeletedFalse(Long id);

    /** Find a patient by their linked user account (for GET /patients/me). */
    Optional<Patient> findByUserIdAndDeletedFalse(Long userId);

    Optional<Patient> findByPhoneAndClinicIdAndDeletedFalse(String phone, Long clinicId);

    boolean existsByClinicIdAndPhoneAndDeletedFalse(Long clinicId, String phone);

    boolean existsByUserIdAndDeletedFalse(Long userId);

    /**
     * Unified search + list query.
     *
     * When q is null or blank, returns all patients for the clinic (no filter).
     * When q is provided, searches across first+last name (case-insensitive),
     * phone number (contains), and email (case-insensitive).
     *
     * Why JPQL over Specification?
     * The search logic is simple and fixed — a Specification API adds complexity
     * without benefit here. JPQL is easier to read and debug.
     */
    @Query("""
            SELECT p FROM Patient p
            WHERE p.clinicId = :clinicId
              AND p.deleted = false
              AND (:activeOnly = false OR p.active = true)
              AND (:q IS NULL OR :q = ''
                   OR LOWER(CONCAT(p.firstName, ' ', COALESCE(p.lastName, ''))) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR p.phone LIKE CONCAT('%', :q, '%')
                   OR LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Patient> searchPatients(
            @Param("clinicId") Long clinicId,
            @Param("q") String q,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable);

    // ── Reporting queries ─────────────────────────────────────────────────────

    long countByClinicIdAndDeletedFalseAndActiveTrue(Long clinicId);

    @Query("""
            SELECT COUNT(p)
            FROM Patient p
            WHERE p.clinicId = :clinicId
              AND p.deleted = false
              AND p.createdAt BETWEEN :from AND :to
            """)
    long countNewRegistrations(@Param("clinicId") Long clinicId,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    /**
     * Count by gender for active, non-deleted patients in a clinic.
     * Returns List of [Gender (may be null), Long count].
     */
    @Query("""
            SELECT p.gender, COUNT(p)
            FROM Patient p
            WHERE p.clinicId = :clinicId
              AND p.deleted = false
            GROUP BY p.gender
            """)
    List<Object[]> countByGender(@Param("clinicId") Long clinicId);
}
