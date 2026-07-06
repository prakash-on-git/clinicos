package com.prakash.clinicos.appointment.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class BookAppointmentRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    /**
     * Optional. If provided, the treatment's effective duration for this doctor
     * is used to block the right number of slots. If null, defaults to 10 minutes.
     */
    private Long treatmentTypeId;

    @NotNull(message = "appointmentDate is required")
    @FutureOrPresent(message = "Cannot book appointments in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    /**
     * Must be on a 10-minute boundary (e.g. 09:00, 09:10, 09:30).
     * Must be a slot returned by GET /doctors/{id}/availability?date=...
     */
    @NotNull(message = "startTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /** Patient's stated reason for the visit / chief complaint. */
    private String reason;

    /** Initial internal notes (optional). */
    private String notes;
}
