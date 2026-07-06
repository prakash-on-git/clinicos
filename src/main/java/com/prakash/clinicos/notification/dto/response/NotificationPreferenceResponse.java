package com.prakash.clinicos.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationPreferenceResponse {

    private Long clinicId;

    private boolean appointmentBookedSms;
    private boolean appointmentBookedEmail;
    private boolean appointmentReminderSms;
    private boolean appointmentReminderEmail;
    private boolean appointmentConfirmedSms;
    private boolean appointmentConfirmedEmail;
    private boolean appointmentCancelledSms;
    private boolean appointmentCancelledEmail;
    private boolean appointmentRescheduledSms;
    private boolean appointmentRescheduledEmail;
    private boolean tokenCalledSms;
    private boolean invoiceIssuedEmail;
    private boolean paymentReceivedSms;
    private boolean paymentReceivedEmail;
}
