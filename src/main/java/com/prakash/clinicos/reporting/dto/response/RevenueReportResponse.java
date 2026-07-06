package com.prakash.clinicos.reporting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueReportResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    /** Sum of totalAmount for non-cancelled, non-refunded invoices. */
    private BigDecimal totalInvoiced;

    /** Sum of amountPaid for the same set. */
    private BigDecimal totalCollected;

    /** Sum of amountDue (outstanding balance). */
    private BigDecimal totalOutstanding;

    /** Sum of totalAmount for REFUNDED invoices. */
    private BigDecimal totalRefunded;

    /** Number of invoices (excluding CANCELLED). */
    private long invoiceCount;

    private List<DoctorRevenue> byDoctor;

    /** Key = payment method name, Value = total amount collected. */
    private Map<String, BigDecimal> byPaymentMethod;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorRevenue {
        private Long doctorId;
        private String doctorName;
        private BigDecimal invoiced;
        private BigDecimal collected;
    }
}
