package com.prakash.clinicos.queue.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.prakash.clinicos.queue.entity.QueueStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueTokenResponse {

    private Long id;
    private Long clinicId;
    private Long doctorId;
    private Long patientId;
    private Long appointmentId;

    // Denormalized names for convenience
    private String doctorName;
    private String patientName;

    /** Display number shown to the patient: "Token #7" */
    private int tokenNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate queueDate;

    private QueueStatus status;

    private String notes;

    /**
     * How many patients are ahead of this one (WAITING tokens with lower token numbers).
     * Null if status is not WAITING (already called or done).
     * Use this to display estimated wait time on a screen.
     */
    private Integer tokensAhead;

    /**
     * Rough estimated wait in minutes (tokensAhead × 10 minutes).
     * Null if status is not WAITING.
     */
    private Integer estimatedWaitMins;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calledAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
