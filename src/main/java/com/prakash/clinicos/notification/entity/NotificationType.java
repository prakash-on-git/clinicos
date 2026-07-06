package com.prakash.clinicos.notification.entity;

public enum NotificationType {
    APPOINTMENT_BOOKED,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_REMINDER,      // sent by scheduler 24 h before
    APPOINTMENT_CANCELLED,
    APPOINTMENT_RESCHEDULED,
    TOKEN_CALLED,
    INVOICE_ISSUED,
    PAYMENT_RECEIVED
}
