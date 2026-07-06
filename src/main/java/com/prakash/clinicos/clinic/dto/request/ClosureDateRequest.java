package com.prakash.clinicos.clinic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClosureDateRequest {

    @NotNull(message = "closureDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closureDate;

    /** Reason shown in booking UI. E.g. "Independence Day", "Annual Staff Training". */
    private String reason;
}
