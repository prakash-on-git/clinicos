package com.prakash.clinicos.billing.repository;

import com.prakash.clinicos.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceIdOrderByPaymentDateAscIdAsc(Long invoiceId);

    /** Sum of all payments recorded against an invoice. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoiceId = :invoiceId")
    BigDecimal sumByInvoiceId(@Param("invoiceId") Long invoiceId);

    /**
     * Per-payment-method breakdown for a clinic in a date range.
     * Returns List of [paymentMethod, SUM(amount)].
     */
    @Query("""
            SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.clinicId = :clinicId
              AND p.paymentDate BETWEEN :from AND :to
            GROUP BY p.paymentMethod
            """)
    List<Object[]> amountByPaymentMethod(@Param("clinicId") Long clinicId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);
}
