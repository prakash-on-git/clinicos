package com.prakash.clinicos.notification.dto.request;

/**
 * All fields are optional — null means "don't change this preference".
 */
public class UpdateNotificationPreferencesRequest {

    private Boolean appointmentBookedSms;
    private Boolean appointmentBookedEmail;
    private Boolean appointmentReminderSms;
    private Boolean appointmentReminderEmail;
    private Boolean appointmentConfirmedSms;
    private Boolean appointmentConfirmedEmail;
    private Boolean appointmentCancelledSms;
    private Boolean appointmentCancelledEmail;
    private Boolean appointmentRescheduledSms;
    private Boolean appointmentRescheduledEmail;
    private Boolean tokenCalledSms;
    private Boolean invoiceIssuedEmail;
    private Boolean paymentReceivedSms;
    private Boolean paymentReceivedEmail;

    public Boolean getAppointmentBookedSms()        { return appointmentBookedSms; }
    public Boolean getAppointmentBookedEmail()       { return appointmentBookedEmail; }
    public Boolean getAppointmentReminderSms()       { return appointmentReminderSms; }
    public Boolean getAppointmentReminderEmail()     { return appointmentReminderEmail; }
    public Boolean getAppointmentConfirmedSms()      { return appointmentConfirmedSms; }
    public Boolean getAppointmentConfirmedEmail()    { return appointmentConfirmedEmail; }
    public Boolean getAppointmentCancelledSms()      { return appointmentCancelledSms; }
    public Boolean getAppointmentCancelledEmail()    { return appointmentCancelledEmail; }
    public Boolean getAppointmentRescheduledSms()    { return appointmentRescheduledSms; }
    public Boolean getAppointmentRescheduledEmail()  { return appointmentRescheduledEmail; }
    public Boolean getTokenCalledSms()               { return tokenCalledSms; }
    public Boolean getInvoiceIssuedEmail()           { return invoiceIssuedEmail; }
    public Boolean getPaymentReceivedSms()           { return paymentReceivedSms; }
    public Boolean getPaymentReceivedEmail()         { return paymentReceivedEmail; }
}
