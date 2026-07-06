package com.prakash.clinicos.doctor.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full doctor profile.
 *
 * Used for: POST /doctors, GET /doctors/{id}, PUT /doctors/{id}
 *
 * weeklySchedule, breaks, and treatments are populated only for single-doctor fetches.
 * They are null in paginated list responses (to avoid N+1 queries).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponse {

    private Long id;
    private Long clinicId;

    /** null if the doctor hasn't linked a user account yet. */
    private Long userId;

    private String fullName;
    private String slug;
    private String email;
    private String phone;
    private String qualification;
    private String specialization;
    private String bio;
    private String avatarUrl;
    private String registrationNumber;
    private BigDecimal consultationFee;
    private boolean active;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Populated only for single-doctor GET — null in list responses
    private List<DoctorWeeklyScheduleResponse> weeklySchedule;
    private List<DoctorBreakResponse> breaks;
    private List<DoctorTreatmentResponse> treatments;
}
