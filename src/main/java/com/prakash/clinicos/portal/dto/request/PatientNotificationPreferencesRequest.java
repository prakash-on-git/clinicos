package com.prakash.clinicos.portal.dto.request;

import lombok.Getter;

/**
 * Allows a patient to opt in or out of SMS and email notifications.
 * Null fields are ignored (patch semantics).
 */
@Getter
public class PatientNotificationPreferencesRequest {

    /** true = patient opts OUT of SMS notifications. */
    private Boolean smsOptOut;

    /** true = patient opts OUT of email notifications. */
    private Boolean emailOptOut;
}
