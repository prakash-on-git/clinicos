package com.prakash.clinicos.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Request body for patient self-registration.
 *
 * The patient must already be registered in the clinic by staff.
 * This endpoint links a login account to that existing patient record.
 *
 * Flow:
 *   1. Look up patient by clinicId + phone.
 *   2. Verify they don't already have a linked user account.
 *   3. Create a user with PATIENT role.
 *   4. Link patient.userId = user.id.
 *   5. Return a JWT so the patient is immediately logged in.
 */
@Getter
public class PatientRegisterRequest {

    @NotNull
    private Long clinicId;

    /** Phone number exactly as registered in the clinic (used to find the patient record). */
    @NotBlank
    private String phone;

    /** Email used for logging in. May differ from the email stored in the patient record. */
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
