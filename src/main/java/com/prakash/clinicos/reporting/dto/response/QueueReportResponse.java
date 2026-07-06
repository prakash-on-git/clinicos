package com.prakash.clinicos.reporting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueReportResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private long totalTokens;
    private long completed;
    private long skipped;
    private long cancelled;
    private long waiting;
    private long called;
    private long inProgress;

    /** Average minutes from token creation to being called (patient wait time). */
    private double averageWaitMinutes;

    /** Average minutes from consultation start to completion. */
    private double averageConsultationMinutes;

    /** Percentage of tokens that were skipped (skipped / totalTokens × 100). */
    private double skipRate;

    private List<DoctorQueue> byDoctor;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorQueue {
        private Long doctorId;
        private String doctorName;
        private long total;
        private long completed;
        private long skipped;
        private double avgWaitMinutes;
        private double avgConsultationMinutes;
    }
}
