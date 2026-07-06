package com.prakash.clinicos.clinic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClinicRequest {

    @NotBlank(message = "Clinic name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Email(message = "Must be a valid email address")
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

    /**
     * IANA timezone ID. Examples: "Asia/Kolkata", "America/New_York", "Europe/London".
     * Validated against ZoneId.of() in the service — invalid values return 400.
     * Defaults to "Asia/Kolkata" if not provided.
     */
    @Size(max = 100)
    private String timezone;
}
