package com.prakash.clinicos.reporting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorPerformanceResponse {

    private Long doctorId;
    private String doctorName;
    private String specialization;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    // ── Appointments ─────────────────────────────────────────────────────────

    private long appointmentsTotal;
    private long appointmentsCompleted;
    private long appointmentsCancelled;
    private long appointmentsNoShow;

    // ── Revenue ──────────────────────────────────────────────────────────────

    private BigDecimal revenueGenerated;
    private BigDecimal revenueCollected;

    // ── Queue / consultation time ─────────────────────────────────────────────

    private long tokensCompleted;
    private double averageWaitMinutes;
    private double averageConsultationMinutes;

    // ── Top treatments ────────────────────────────────────────────────────────

    private List<TreatmentCount> topTreatments;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TreatmentCount {
        private Long treatmentTypeId;
        private String treatmentName;
        private long count;
    }
}
