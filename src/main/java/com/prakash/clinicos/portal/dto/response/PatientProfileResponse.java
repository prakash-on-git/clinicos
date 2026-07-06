package com.prakash.clinicos.portal.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.prakash.clinicos.patient.entity.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Patient-facing view of their own profile.
 * Internal fields (notes, deletedBy, createdBy) are excluded.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientProfileResponse {

    private Long id;
    private Long clinicId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;
    private String bloodGroup;
    private String allergies;
    private String chronicConditions;
    private String currentMedications;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String address;
    private boolean smsOptOut;
    private boolean emailOptOut;
}
