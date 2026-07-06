package com.prakash.clinicos.clinic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * All fields are optional. The service only updates fields that are non-null.
 * Omit a field (or send null) to leave it unchanged.
 *
 * Also controls alwaysOpen (24/7 toggle) here — not a separate endpoint —
 * since it is a property of the clinic profile, not an emergency action.
 */
@Getter
@Setter
public class UpdateClinicRequest {

    @Size(min = 1, max = 255)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 30)
    private String phone;

    @Email
    private String email;

    @Size(max = 512)
    private String website;

    private String addressLine1;
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String timezone;

    /** Toggle 24/7 mode. null = no change. */
    private Boolean alwaysOpen;
}
