package com.prakash.clinicos.patient.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.prakash.clinicos.patient.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreatePatientRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    /**
     * Phone is required for patient identification.
     * Unique per clinic — prevents duplicate registrations.
     */
    @NotBlank(message = "Phone number is required")
    @Size(max = 30)
    private String phone;

    private String email;

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;

    // ── Medical background ────────────────────────────────────────────────────

    /** e.g. "A+", "O-", "AB+" */
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-")
    private String bloodGroup;

    /** Free text: "Penicillin, Aspirin" */
    private String allergies;

    /** Free text: "Diabetes Type 2, Hypertension" */
    private String chronicConditions;

    /** Free text: "Metformin 500mg" */
    private String currentMedications;

    // ── Emergency contact ─────────────────────────────────────────────────────

    @Size(max = 255)
    private String emergencyContactName;

    @Size(max = 30)
    private String emergencyContactPhone;

    /** e.g. "Mother", "Spouse", "Guardian" */
    @Size(max = 100)
    private String emergencyContactRelation;

    // ── Other ─────────────────────────────────────────────────────────────────

    private String address;

    /** Internal staff notes. */
    private String notes;

    /**
     * Optional: link this patient to an existing user account at registration time.
     * More commonly done later via the link-user endpoint.
     */
    private Long userId;
}
