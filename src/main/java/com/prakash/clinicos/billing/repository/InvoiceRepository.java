package com.prakash.clinicos.billing.repository;

import com.prakash.clinicos.billing.entity.Invoice;
import com.prakash.clinicos.billing.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByIdAndClinicId(Long id, Long clinicId);

    boolean existsByAppointmentId(Long appointmentId);

    /**
     * Next sequence number for invoice_number generation within a clinic.
     * Parses the numeric suffix from "INV-YYYY-NNNNN".
     */
    @Query("""
            SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 10) AS integer)), 0) + 1
            FROM Invoice i
            WHERE i.clinicId = :clinicId
            AND i.invoiceNumber LIKE CONCAT('INV-', :year, '-%')
            """)
    int getNextSequenceForYear(@Param("clinicId") Long clinicId, @Param("year") int year);

    /**
     * Multi-filter search — all params optional except clinicId.
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.clinicId = :clinicId
            AND (:patientId IS NULL OR i.patientId = :patientId)
            AND (:doctorId  IS NULL OR i.doctorId  = :doctorId)
            AND (:status    IS NULL OR i.status    = :status)
            AND (:fromDate  IS NULL OR i.invoiceDate >= :fromDate)
            AND (:toDate    IS NULL OR i.invoiceDate <= :toDate)
            ORDER BY i.invoiceDate DESC, i.id DESC
            """)
    Page<Invoice> search(
            @Param("clinicId")  Long clinicId,
            @Param("patientId") Long patientId,
            @Param("doctorId")  Long doctorId,
            @Param("status")    InvoiceStatus status,
            @Param("fromDate")  LocalDate fromDate,
            @Param("toDate")    LocalDate toDate,
            Pageable pageable
    );

    // ── Reporting queries ─────────────────────────────────────────────────────

    /**
     * Returns [SUM(totalAmount), SUM(amountPaid), SUM(amountDue), COUNT(*)]
     * for invoices in the date range, excluding the given statuses.
     */
    @Query("""
            SELECT COALESCE(SUM(i.totalAmount), 0),
                   COALESCE(SUM(i.amountPaid), 0),
                   COALESCE(SUM(i.amountDue), 0),
                   COUNT(i)
            FROM Invoice i
            WHERE i.clinicId = :clinicId
              AND i.invoiceDate BETWEEN :from AND :to
              AND i.status NOT IN :excluded
            """)
    Object[] revenueSummary(@Param("clinicId") Long clinicId,
                            @Param("from") LocalDate from,
                            @Param("to") LocalDate to,
                            @Param("excluded") Collection<InvoiceStatus> excluded);

    /** Total invoice amount for a single status (e.g. REFUNDED). */
    @Query("""
            SELECT COALESCE(SUM(i.totalAmount), 0)
            FROM Invoice i
            WHERE i.clinicId = :clinicId
              AND i.invoiceDate BETWEEN :from AND :to
              AND i.status = :status
            """)
    BigDecimal totalByStatus(@Param("clinicId") Long clinicId,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to,
                             @Param("status") InvoiceStatus status);

    /**
     * Per-doctor breakdown.
     * Returns List of [doctorId, SUM(totalAmount), SUM(amountPaid)].
     */
    @Query("""
            SELECT i.doctorId,
                   COALESCE(SUM(i.totalAmount), 0),
                   COALESCE(SUM(i.amountPaid), 0)
            FROM Invoice i
            WHERE i.clinicId = :clinicId
              AND i.invoiceDate BETWEEN :from AND :to
              AND i.status NOT IN :excluded
            GROUP BY i.doctorId
            """)
    List<Object[]> revenueByDoctor(@Param("clinicId") Long clinicId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to,
                                   @Param("excluded") Collection<InvoiceStatus> excluded);
}
