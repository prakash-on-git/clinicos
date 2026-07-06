package com.prakash.clinicos.doctor.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDoctorRequest {

    @NotBlank(message = "Doctor's full name is required")
    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;

    @Email(message = "Must be a valid email address")
    private String email;

    @Size(max = 30)
    private String phone;

    /** e.g. "MBBS, MD (Cardiology)" */
    @Size(max = 255)
    private String qualification;

    /** e.g. "Cardiologist" */
    @Size(max = 255)
    private String specialization;

    private String bio;

    @Size(max = 512)
    private String avatarUrl;

    @Size(max = 100)
    private String registrationNumber;

    @DecimalMin(value = "0.00", message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    /**
     * Optional: link to an existing user account with the DOCTOR role.
     * If provided, the user must exist and must not already be linked to another doctor.
     */
    private Long userId;
}
