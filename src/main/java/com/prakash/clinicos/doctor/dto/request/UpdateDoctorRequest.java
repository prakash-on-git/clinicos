package com.prakash.clinicos.doctor.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * All fields are optional — only non-null values are applied (partial update).
 * Send only the fields you want to change.
 */
@Getter
@Setter
public class UpdateDoctorRequest {

    @Size(min = 1, max = 255)
    private String fullName;

    @Email
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String qualification;

    @Size(max = 255)
    private String specialization;

    private String bio;

    @Size(max = 512)
    private String avatarUrl;

    @Size(max = 100)
    private String registrationNumber;

    @DecimalMin("0.00")
    private BigDecimal consultationFee;

    /**
     * Toggle active/inactive. null = no change.
     * Inactive doctors are hidden from booking pages but still visible to admin.
     */
    private Boolean active;
}
