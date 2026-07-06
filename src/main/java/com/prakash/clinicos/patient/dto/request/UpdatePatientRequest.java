package com.prakash.clinicos.patient.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.prakash.clinicos.patient.entity.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

/**
 * All fields are optional (patch semantics).
 * Only non-null fields are applied in the service.
 */
@Getter
public class UpdatePatientRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 30)
    private String phone;

    private String email;

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-")
    private String bloodGroup;

    private String allergies;

    private String chronicConditions;

    private String currentMedications;

    private String emergencyContactName;

    @Size(max = 30)
    private String emergencyContactPhone;

    @Size(max = 100)
    private String emergencyContactRelation;

    private String address;

    private String notes;

    /** Set to false to mark a patient as inactive (not deleted). */
    private Boolean active;
}
