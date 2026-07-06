package com.prakash.clinicos.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub email provider — logs the message and simulates success.
 *
 * To go live, replace this class body with calls to SendGrid / AWS SES / JavaMailSender.
 * The rest of the notification pipeline stays unchanged.
 *
 * Production wiring example (Spring Mail):
 *   MimeMessage msg = mailSender.createMimeMessage();
 *   helper.setTo(recipient); helper.setSubject(subject); helper.setText(message, true);
 *   mailSender.send(msg);
 */
@Component("emailProvider")
@Slf4j
public class EmailNotificationProvider implements NotificationProvider {

    @Override
    public boolean send(String recipient, String subject, String message) {
        // TODO: replace with real email provider call
        log.info("[EMAIL STUB] To: {} | Subject: {} | Body: {}", recipient, subject, message);
        return true;
    }
}
