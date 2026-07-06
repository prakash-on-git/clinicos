package com.prakash.clinicos.appointment.dto.request;

import lombok.Getter;

@Getter
public class CancelAppointmentRequest {

    /** Optional: reason for cancellation. */
    private String reason;
}
