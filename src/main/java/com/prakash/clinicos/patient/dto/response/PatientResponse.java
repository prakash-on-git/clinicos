package com.prakash.clinicos.patient.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.prakash.clinicos.patient.entity.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient response DTO.
 *
 * @JsonInclude(NON_NULL) keeps the JSON clean — optional fields that aren't set
 * (bloodGroup, allergies, etc.) are omitted from the response rather than shown as null.
 * This reduces payload size, especially for list responses.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientResponse {

    private Long id;
    private Long clinicId;
    private Long userId;

    private String firstName;
    private String lastName;

    /** Computed full name for convenience. */
    private String fullName;

    private String phone;
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;

    // ── Medical background ────────────────────────────────────────────────────
    private String bloodGroup;
    private String allergies;
    private String chronicConditions;
    private String currentMedications;

    // ── Emergency contact ─────────────────────────────────────────────────────
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;

    // ── Other ─────────────────────────────────────────────────────────────────
    private String address;

    /**
     * Internal notes are included in the full single-patient response
     * but omitted in list responses (set to null in toSummaryResponse).
     */
    private String notes;

    private boolean active;
    private boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
