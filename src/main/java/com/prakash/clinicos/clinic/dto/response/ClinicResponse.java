package com.prakash.clinicos.clinic.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full clinic response.
 *
 * Used for: POST /clinics, GET /clinics/{id}, GET /clinics/mine, PUT /clinics/{id}
 *
 * @JsonInclude(NON_NULL): businessHours, closureDates, settings are null in paginated
 * list responses (to avoid N+1 queries). They're only populated for single-clinic fetches.
 *
 * isCurrentlyOpen: computed in real-time from business hours + closure dates + flags.
 * Never stored in DB — changes minute to minute.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClinicResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;

    // Contact
    private String phone;
    private String email;
    private String website;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Operational
    private String timezone;
    private Long ownerUserId;
    private boolean alwaysOpen;
    private boolean emergencyClosed;
    private String emergencyCloseReason;
    private LocalDateTime emergencyClosedAt;
    private boolean deleted;

    /**
     * Real-time computation:
     *   false if deleted, emergency closed, or today is a closure date
     *   true  if 24/7 and not emergency closed
     *   otherwise: checks if current local time falls within any shift window for today
     */
    private boolean currentlyOpen;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Populated only for single-clinic fetches, null in paginated list
    private List<BusinessHoursResponse> businessHours;
    private List<ClosureDateResponse> upcomingClosures;
    private ClinicSettingsResponse settings;
}
