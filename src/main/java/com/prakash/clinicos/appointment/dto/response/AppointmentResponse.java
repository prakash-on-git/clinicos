package com.prakash.clinicos.appointment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment response DTO.
 * @JsonInclude(NON_NULL) omits null optional fields (cancellationReason, rescheduledFromId, etc.)
 * to keep the response clean in list views.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponse {

    private Long id;
    private Long clinicId;
    private Long doctorId;
    private Long patientId;
    private Long treatmentTypeId;

    // Denormalized names for convenience (avoids extra client-side joins)
    private String doctorName;
    private String patientName;
    private String treatmentName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private int durationMins;

    private AppointmentStatus status;

    private String reason;
    private String notes;

    // Cancellation details (null unless status = CANCELLED)
    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

    // Reschedule chain (null unless this appointment was rescheduled)
    private Long rescheduledFromId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
