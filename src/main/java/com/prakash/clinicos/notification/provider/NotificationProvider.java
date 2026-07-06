package com.prakash.clinicos.notification.provider;

/**
 * Contract for outbound notification delivery.
 *
 * Implementations swap in the real external service (Twilio for SMS,
 * SendGrid / AWS SES for email) without changing callers.
 *
 * @param recipient  Phone number (E.164 format) or email address.
 * @param subject    Subject line — used for email, ignored by SMS.
 * @param message    Body text.
 * @return true if the provider accepted the message, false on failure.
 */
public interface NotificationProvider {
    boolean send(String recipient, String subject, String message);
}
