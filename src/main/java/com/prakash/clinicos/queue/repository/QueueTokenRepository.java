package com.prakash.clinicos.queue.repository;

import com.prakash.clinicos.queue.entity.QueueStatus;
import com.prakash.clinicos.queue.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    Optional<QueueToken> findByIdAndClinicId(Long id, Long clinicId);

    boolean existsByAppointmentId(Long appointmentId);

    /** Today's full queue for a clinic, all doctors, ordered by token number. */
    List<QueueToken> findByClinicIdAndQueueDateOrderByDoctorIdAscTokenNumberAsc(
            Long clinicId, LocalDate date);

    /** Today's queue for a specific doctor. */
    List<QueueToken> findByClinicIdAndDoctorIdAndQueueDateOrderByTokenNumberAsc(
            Long clinicId, Long doctorId, LocalDate date);

    /** All tokens with a specific status today. */
    List<QueueToken> findByClinicIdAndQueueDateAndStatusOrderByDoctorIdAscTokenNumberAsc(
            Long clinicId, LocalDate date, QueueStatus status);

    /** All tokens with a specific status for a doctor today. */
    List<QueueToken> findByClinicIdAndDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
            Long clinicId, Long doctorId, LocalDate date, QueueStatus status);

    /**
     * Next token number for a doctor today.
     * Returns MAX(token_number) + 1, or 1 if no tokens exist yet.
     * Uses COALESCE so the first token of the day is always 1.
     */
    @Query("""
            SELECT COALESCE(MAX(q.tokenNumber), 0) + 1
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.doctorId = :doctorId
              AND q.queueDate = :date
            """)
    int getNextTokenNumber(@Param("clinicId") Long clinicId,
                           @Param("doctorId") Long doctorId,
                           @Param("date") LocalDate date);

    /**
     * Count WAITING tokens with a lower token number (= people ahead in queue).
     * Used to compute estimated wait time.
     */
    @Query("""
            SELECT COUNT(q)
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.doctorId = :doctorId
              AND q.queueDate = :date
              AND q.status = 'WAITING'
              AND q.tokenNumber < :tokenNumber
            """)
    int countTokensAhead(@Param("clinicId") Long clinicId,
                         @Param("doctorId") Long doctorId,
                         @Param("date") LocalDate date,
                         @Param("tokenNumber") int tokenNumber);

    /**
     * Highest token number currently in queue today (for recall: re-add skipped patient at end).
     */
    @Query("""
            SELECT COALESCE(MAX(q.tokenNumber), 0)
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.doctorId = :doctorId
              AND q.queueDate = :date
            """)
    int getMaxTokenNumber(@Param("clinicId") Long clinicId,
                          @Param("doctorId") Long doctorId,
                          @Param("date") LocalDate date);

    // ── Reporting queries ─────────────────────────────────────────────────────

    /**
     * Count per status for a clinic in a date range.
     * Returns List of [QueueStatus, Long count].
     */
    @Query("""
            SELECT q.status, COUNT(q)
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.queueDate BETWEEN :from AND :to
            GROUP BY q.status
            """)
    List<Object[]> countByStatusForRange(@Param("clinicId") Long clinicId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    /**
     * Count per doctor per status for a clinic in a date range.
     * Returns List of [doctorId, QueueStatus, Long count].
     */
    @Query("""
            SELECT q.doctorId, q.status, COUNT(q)
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.queueDate BETWEEN :from AND :to
            GROUP BY q.doctorId, q.status
            """)
    List<Object[]> countByDoctorAndStatusForRange(@Param("clinicId") Long clinicId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    /**
     * All COMPLETED tokens with timestamps for a clinic and date range.
     * Used to compute average wait time (calledAt − createdAt)
     * and consultation time (completedAt − startedAt) in Java.
     * Returns List of [doctorId, createdAt, calledAt, startedAt, completedAt].
     */
    @Query("""
            SELECT q.doctorId, q.createdAt, q.calledAt, q.startedAt, q.completedAt
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.queueDate BETWEEN :from AND :to
              AND q.status = 'COMPLETED'
              AND q.calledAt IS NOT NULL
              AND q.startedAt IS NOT NULL
              AND q.completedAt IS NOT NULL
            """)
    List<Object[]> completedTokenTimestamps(@Param("clinicId") Long clinicId,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);

    /**
     * Same but filtered to one doctor — used by the per-doctor performance report.
     */
    @Query("""
            SELECT q.doctorId, q.createdAt, q.calledAt, q.startedAt, q.completedAt
            FROM QueueToken q
            WHERE q.clinicId = :clinicId
              AND q.doctorId = :doctorId
              AND q.queueDate BETWEEN :from AND :to
              AND q.status = 'COMPLETED'
              AND q.calledAt IS NOT NULL
              AND q.startedAt IS NOT NULL
              AND q.completedAt IS NOT NULL
            """)
    List<Object[]> completedTokenTimestampsByDoctor(@Param("clinicId") Long clinicId,
                                                    @Param("doctorId") Long doctorId,
                                                    @Param("from") LocalDate from,
                                                    @Param("to") LocalDate to);
}
