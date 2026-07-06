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
public class AppointmentReportResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private long total;
    private long completed;
    private long confirmed;
    private long pending;
    private long inProgress;
    private long cancelled;
    private long noShow;
    private long rescheduled;

    /**
     * Completion rate as a percentage: completed / (total − rescheduled) × 100.
     * Rescheduled are excluded because they result in a new appointment — not a loss.
     */
    private double completionRate;

    private List<DoctorAppointments> byDoctor;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorAppointments {
        private Long doctorId;
        private String doctorName;
        private long total;
        private long completed;
        private long cancelled;
        private long noShow;
    }
}
