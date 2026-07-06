package com.prakash.clinicos.appointment.repository;

import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByIdAndClinicId(Long id, Long clinicId);

    /**
     * All active (non-terminal) appointments for a doctor on a specific date.
     * Used by DoctorAvailabilityService to subtract booked slots and by the
     * conflict check during new booking.
     *
     * CANCELLED, RESCHEDULED, NO_SHOW are excluded because they free the slot.
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.appointmentDate = :date
              AND a.status NOT IN ('CANCELLED', 'RESCHEDULED', 'NO_SHOW')
            ORDER BY a.startTime
            """)
    List<Appointment> findActiveByDoctorAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);

    /**
     * Interval overlap check for double-booking prevention.
     *
     * Two time intervals [s1,e1) and [s2,e2) overlap iff s1 < e2 AND e1 > s2.
     * We exclude terminal statuses that free the slot.
     */
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.appointmentDate = :date
              AND a.status NOT IN ('CANCELLED', 'RESCHEDULED', 'NO_SHOW')
              AND a.startTime < :endTime
              AND a.endTime > :startTime
            """)
    boolean existsConflictingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Flexible list / search query. All params are optional.
     * Null params are treated as "no filter".
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.clinicId = :clinicId
              AND (:doctorId IS NULL OR a.doctorId = :doctorId)
              AND (:patientId IS NULL OR a.patientId = :patientId)
              AND (:date IS NULL OR a.appointmentDate = :date)
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<Appointment> searchAppointments(
            @Param("clinicId") Long clinicId,
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId,
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status,
            Pageable pageable);

    /** Patient appointment history (all clinics, most recent first). */
    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    /** Paginated variant — used by the patient portal. */
    Page<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId, Pageable pageable);

    /** Doctor's full day schedule (all statuses). */
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTime(Long doctorId, LocalDate date);

    /** All PENDING/CONFIRMED appointments on a given date — used by the 24h reminder scheduler. */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.appointmentDate = :date
              AND a.status IN ('PENDING', 'CONFIRMED')
            """)
    List<Appointment> findPendingOrConfirmedByDate(@Param("date") LocalDate date);

    // ── Reporting queries ─────────────────────────────────────────────────────

    /**
     * Count per status for a clinic in a date range.
     * Returns List of [AppointmentStatus, Long count].
     */
    @Query("""
            SELECT a.status, COUNT(a)
            FROM Appointment a
            WHERE a.clinicId = :clinicId
              AND a.appointmentDate BETWEEN :from AND :to
            GROUP BY a.status
            """)
    List<Object[]> countByStatusForRange(@Param("clinicId") Long clinicId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    /**
     * Count per doctor per status for a clinic in a date range.
     * Returns List of [doctorId, AppointmentStatus, Long count].
     */
    @Query("""
            SELECT a.doctorId, a.status, COUNT(a)
            FROM Appointment a
            WHERE a.clinicId = :clinicId
              AND a.appointmentDate BETWEEN :from AND :to
            GROUP BY a.doctorId, a.status
            """)
    List<Object[]> countByDoctorAndStatusForRange(@Param("clinicId") Long clinicId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    /**
     * Top treatments for a doctor (by appointment count, completed only).
     * Returns List of [treatmentTypeId, Long count].
     */
    @Query("""
            SELECT a.treatmentTypeId, COUNT(a)
            FROM Appointment a
            WHERE a.clinicId = :clinicId
              AND a.doctorId = :doctorId
              AND a.appointmentDate BETWEEN :from AND :to
              AND a.status = :status
              AND a.treatmentTypeId IS NOT NULL
            GROUP BY a.treatmentTypeId
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> topTreatmentsByDoctor(@Param("clinicId") Long clinicId,
                                          @Param("doctorId") Long doctorId,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to,
                                          @Param("status") AppointmentStatus status);
}
