package com.prakash.clinicos.clinic.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmergencyCloseRequest {

    /** Optional reason. Shown to staff. E.g. "Power outage", "Doctor emergency". */
    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;
}
