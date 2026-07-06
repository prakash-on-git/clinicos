package com.prakash.clinicos.reporting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientReportResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    /** Total active, non-deleted patients in the clinic (all time). */
    private long totalActive;

    /** New patient registrations within the fromDate–toDate window. */
    private long newRegistrations;

    /**
     * Patient count broken down by gender.
     * Keys: MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY, UNKNOWN (for null gender).
     */
    private Map<String, Long> byGender;
}
