package com.prakash.clinicos.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub SMS provider — logs the message and simulates success.
 *
 * To go live, replace this class body with calls to Twilio / MSG91 / AWS SNS.
 * The rest of the notification pipeline (preference checks, logging) stays unchanged.
 *
 * Production wiring example (Twilio):
 *   Message.creator(new PhoneNumber(recipient), new PhoneNumber(FROM), message).create();
 */
@Component("smsProvider")
@Slf4j
public class SmsNotificationProvider implements NotificationProvider {

    @Override
    public boolean send(String recipient, String subject, String message) {
        // TODO: replace with real SMS gateway call
        log.info("[SMS STUB] To: {} | Message: {}", recipient, message);
        return true;
    }
}
