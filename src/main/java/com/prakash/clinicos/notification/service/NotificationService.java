package com.prakash.clinicos.notification.service;

import com.prakash.clinicos.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.prakash.clinicos.notification.dto.response.NotificationLogResponse;
import com.prakash.clinicos.notification.dto.response.NotificationPreferenceResponse;
import com.prakash.clinicos.notification.entity.*;
import com.prakash.clinicos.notification.provider.NotificationProvider;
import com.prakash.clinicos.notification.repository.NotificationLogRepository;
import com.prakash.clinicos.notification.repository.NotificationPreferenceRepository;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Core notification service.
 *
 * Callers (AppointmentService, QueueService, BillingService, scheduler) use the
 * typed send-* methods. Each method:
 *   1. Loads the patient's contact info.
 *   2. Loads (or creates) the clinic's notification preferences.
 *   3. Builds the message from a template.
 *   4. Delegates to the appropriate provider (SMS / email).
 *   5. Persists a NotificationLog record regardless of outcome.
 *
 * Notification failures NEVER propagate to callers — they are swallowed here
 * and recorded in the log with status=FAILED. The main business operation
 * must never roll back because of a failed notification.
 */
@Service
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private final NotificationLogRepository logRepository;
    private final NotificationPreferenceRepository prefRepository;
    private final PatientRepository patientRepository;
    private final NotificationProvider smsProvider;
    private final NotificationProvider emailProvider;

    public NotificationService(NotificationLogRepository logRepository,
                               NotificationPreferenceRepository prefRepository,
                               PatientRepository patientRepository,
                               @Qualifier("smsProvider")   NotificationProvider smsProvider,
                               @Qualifier("emailProvider") NotificationProvider emailProvider) {
        this.logRepository = logRepository;
        this.prefRepository = prefRepository;
        this.patientRepository = patientRepository;
        this.smsProvider = smsProvider;
        this.emailProvider = emailProvider;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Typed send methods — called by other services
    // ════════════════════════════════════════════════════════════════════════

    public void notifyAppointmentBooked(Long clinicId, Long appointmentId,
                                         Long patientId, String doctorName,
                                         LocalDate date, LocalTime time) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", your appointment with Dr. " + doctorName
                    + " is confirmed for " + date.format(DATE_FMT)
                    + " at " + time.format(TIME_FMT) + ". Thank you!";

            if (pref.isAppointmentBookedSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_BOOKED, NotificationChannel.SMS,
                        p.getPhone(), null, msg, appointmentId, "APPOINTMENT");

            if (pref.isAppointmentBookedEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_BOOKED, NotificationChannel.EMAIL,
                        p.getEmail(), "Appointment Confirmation", msg, appointmentId, "APPOINTMENT");
        } catch (Exception ex) {
            log.warn("notifyAppointmentBooked failed silently: {}", ex.getMessage());
        }
    }

    public void notifyAppointmentConfirmed(Long clinicId, Long appointmentId,
                                            Long patientId, String doctorName,
                                            LocalDate date, LocalTime time) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", your appointment with Dr. " + doctorName
                    + " on " + date.format(DATE_FMT) + " at " + time.format(TIME_FMT)
                    + " has been confirmed by the clinic.";

            if (pref.isAppointmentConfirmedSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_CONFIRMED, NotificationChannel.SMS,
                        p.getPhone(), null, msg, appointmentId, "APPOINTMENT");

            if (pref.isAppointmentConfirmedEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_CONFIRMED, NotificationChannel.EMAIL,
                        p.getEmail(), "Appointment Confirmed", msg, appointmentId, "APPOINTMENT");
        } catch (Exception ex) {
            log.warn("notifyAppointmentConfirmed failed silently: {}", ex.getMessage());
        }
    }

    public void notifyAppointmentReminder(Long clinicId, Long appointmentId,
                                           Long patientId, String doctorName,
                                           LocalDate date, LocalTime time) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Reminder: Hi " + firstName(p) + ", you have an appointment with Dr. "
                    + doctorName + " tomorrow (" + date.format(DATE_FMT)
                    + ") at " + time.format(TIME_FMT) + ". Please be on time.";

            if (pref.isAppointmentReminderSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_REMINDER, NotificationChannel.SMS,
                        p.getPhone(), null, msg, appointmentId, "APPOINTMENT");

            if (pref.isAppointmentReminderEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_REMINDER, NotificationChannel.EMAIL,
                        p.getEmail(), "Appointment Reminder", msg, appointmentId, "APPOINTMENT");
        } catch (Exception ex) {
            log.warn("notifyAppointmentReminder failed silently: {}", ex.getMessage());
        }
    }

    public void notifyAppointmentCancelled(Long clinicId, Long appointmentId,
                                            Long patientId, String doctorName,
                                            LocalDate date) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", your appointment with Dr. " + doctorName
                    + " on " + date.format(DATE_FMT) + " has been cancelled. "
                    + "Please contact us to reschedule.";

            if (pref.isAppointmentCancelledSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_CANCELLED, NotificationChannel.SMS,
                        p.getPhone(), null, msg, appointmentId, "APPOINTMENT");

            if (pref.isAppointmentCancelledEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_CANCELLED, NotificationChannel.EMAIL,
                        p.getEmail(), "Appointment Cancelled", msg, appointmentId, "APPOINTMENT");
        } catch (Exception ex) {
            log.warn("notifyAppointmentCancelled failed silently: {}", ex.getMessage());
        }
    }

    public void notifyAppointmentRescheduled(Long clinicId, Long newAppointmentId,
                                              Long patientId, String doctorName,
                                              LocalDate newDate, LocalTime newTime) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", your appointment with Dr. " + doctorName
                    + " has been rescheduled to " + newDate.format(DATE_FMT)
                    + " at " + newTime.format(TIME_FMT) + ".";

            if (pref.isAppointmentRescheduledSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_RESCHEDULED, NotificationChannel.SMS,
                        p.getPhone(), null, msg, newAppointmentId, "APPOINTMENT");

            if (pref.isAppointmentRescheduledEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.APPOINTMENT_RESCHEDULED, NotificationChannel.EMAIL,
                        p.getEmail(), "Appointment Rescheduled", msg, newAppointmentId, "APPOINTMENT");
        } catch (Exception ex) {
            log.warn("notifyAppointmentRescheduled failed silently: {}", ex.getMessage());
        }
    }

    public void notifyTokenCalled(Long clinicId, Long tokenId,
                                   Long patientId, int tokenNumber, String doctorName) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", Token #" + tokenNumber
                    + " is now being called. Please proceed to Dr. " + doctorName + "'s room.";

            if (pref.isTokenCalledSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.TOKEN_CALLED, NotificationChannel.SMS,
                        p.getPhone(), null, msg, tokenId, "QUEUE_TOKEN");
        } catch (Exception ex) {
            log.warn("notifyTokenCalled failed silently: {}", ex.getMessage());
        }
    }

    public void notifyInvoiceIssued(Long clinicId, Long invoiceId,
                                     Long patientId, String invoiceNumber,
                                     BigDecimal totalAmount) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", your invoice " + invoiceNumber
                    + " for ₹" + totalAmount + " has been issued. "
                    + "Please contact us to make a payment.";

            if (pref.isInvoiceIssuedEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.INVOICE_ISSUED, NotificationChannel.EMAIL,
                        p.getEmail(), "Invoice " + invoiceNumber + " Issued", msg,
                        invoiceId, "INVOICE");
        } catch (Exception ex) {
            log.warn("notifyInvoiceIssued failed silently: {}", ex.getMessage());
        }
    }

    public void notifyPaymentReceived(Long clinicId, Long invoiceId,
                                       Long patientId, String invoiceNumber,
                                       BigDecimal amountPaid, BigDecimal amountDue) {
        try {
            Patient p = patient(patientId);
            if (p == null) return;
            NotificationPreference pref = prefs(clinicId);
            String msg = "Hi " + firstName(p) + ", we have received your payment of ₹"
                    + amountPaid + " for invoice " + invoiceNumber + ". "
                    + (amountDue.compareTo(BigDecimal.ZERO) == 0
                        ? "Your invoice is now fully paid. Thank you!"
                        : "Remaining balance: ₹" + amountDue + ".");

            if (pref.isPaymentReceivedSms() && !p.isSmsOptOut())
                dispatch(clinicId, NotificationType.PAYMENT_RECEIVED, NotificationChannel.SMS,
                        p.getPhone(), null, msg, invoiceId, "INVOICE");

            if (pref.isPaymentReceivedEmail() && p.getEmail() != null && !p.isEmailOptOut())
                dispatch(clinicId, NotificationType.PAYMENT_RECEIVED, NotificationChannel.EMAIL,
                        p.getEmail(), "Payment Received – " + invoiceNumber, msg,
                        invoiceId, "INVOICE");
        } catch (Exception ex) {
            log.warn("notifyPaymentReceived failed silently: {}", ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Preference management
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(Long clinicId) {
        return toPreferenceResponse(prefs(clinicId));
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(Long clinicId,
                                                             UpdateNotificationPreferencesRequest req) {
        NotificationPreference pref = prefs(clinicId);
        if (req.getAppointmentBookedSms()        != null) pref.setAppointmentBookedSms(req.getAppointmentBookedSms());
        if (req.getAppointmentBookedEmail()       != null) pref.setAppointmentBookedEmail(req.getAppointmentBookedEmail());
        if (req.getAppointmentReminderSms()       != null) pref.setAppointmentReminderSms(req.getAppointmentReminderSms());
        if (req.getAppointmentReminderEmail()     != null) pref.setAppointmentReminderEmail(req.getAppointmentReminderEmail());
        if (req.getAppointmentConfirmedSms()      != null) pref.setAppointmentConfirmedSms(req.getAppointmentConfirmedSms());
        if (req.getAppointmentConfirmedEmail()    != null) pref.setAppointmentConfirmedEmail(req.getAppointmentConfirmedEmail());
        if (req.getAppointmentCancelledSms()      != null) pref.setAppointmentCancelledSms(req.getAppointmentCancelledSms());
        if (req.getAppointmentCancelledEmail()    != null) pref.setAppointmentCancelledEmail(req.getAppointmentCancelledEmail());
        if (req.getAppointmentRescheduledSms()    != null) pref.setAppointmentRescheduledSms(req.getAppointmentRescheduledSms());
        if (req.getAppointmentRescheduledEmail()  != null) pref.setAppointmentRescheduledEmail(req.getAppointmentRescheduledEmail());
        if (req.getTokenCalledSms()               != null) pref.setTokenCalledSms(req.getTokenCalledSms());
        if (req.getInvoiceIssuedEmail()           != null) pref.setInvoiceIssuedEmail(req.getInvoiceIssuedEmail());
        if (req.getPaymentReceivedSms()           != null) pref.setPaymentReceivedSms(req.getPaymentReceivedSms());
        if (req.getPaymentReceivedEmail()         != null) pref.setPaymentReceivedEmail(req.getPaymentReceivedEmail());
        return toPreferenceResponse(prefRepository.save(pref));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Log querying
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Page<NotificationLogResponse> getLogs(Long clinicId,
                                                  NotificationType type,
                                                  NotificationChannel channel,
                                                  NotificationStatus status,
                                                  LocalDateTime from,
                                                  LocalDateTime to,
                                                  Pageable pageable) {
        return logRepository.search(clinicId, type, channel, status, from, to, pageable)
                .map(this::toLogResponse);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Internal helpers
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Core dispatch — calls the provider and writes a NotificationLog.
     * All exceptions are caught here to ensure the log is always written.
     */
    private void dispatch(Long clinicId, NotificationType type, NotificationChannel channel,
                           String recipient, String subject, String message,
                           Long referenceId, String referenceType) {
        if (recipient == null || recipient.isBlank()) {
            saveLog(clinicId, type, channel, "UNKNOWN", subject, message,
                    NotificationStatus.SKIPPED, "No recipient contact", referenceId, referenceType, null);
            return;
        }

        NotificationProvider provider = channel == NotificationChannel.SMS ? smsProvider : emailProvider;
        LocalDateTime sentAt = null;
        NotificationStatus result;
        String errorReason = null;

        try {
            boolean ok = provider.send(recipient, subject, message);
            result = ok ? NotificationStatus.SENT : NotificationStatus.FAILED;
            if (ok) sentAt = LocalDateTime.now();
            else errorReason = "Provider returned false";
        } catch (Exception ex) {
            result = NotificationStatus.FAILED;
            errorReason = ex.getMessage();
            log.error("Notification dispatch failed [{} {}]: {}", channel, type, ex.getMessage());
        }

        saveLog(clinicId, type, channel, recipient, subject, message,
                result, errorReason, referenceId, referenceType, sentAt);
    }

    private void saveLog(Long clinicId, NotificationType type, NotificationChannel channel,
                          String recipient, String subject, String message,
                          NotificationStatus status, String errorReason,
                          Long referenceId, String referenceType, LocalDateTime sentAt) {
        try {
            logRepository.save(NotificationLog.builder()
                    .clinicId(clinicId)
                    .type(type)
                    .channel(channel)
                    .recipient(recipient)
                    .subject(subject)
                    .message(message)
                    .status(status)
                    .errorReason(errorReason)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .sentAt(sentAt)
                    .build());
        } catch (Exception ex) {
            log.error("Failed to save notification log: {}", ex.getMessage());
        }
    }

    /** Loads (or lazily creates with defaults) the clinic's preferences. */
    private NotificationPreference prefs(Long clinicId) {
        return prefRepository.findByClinicId(clinicId)
                .orElseGet(() -> prefRepository.save(
                        NotificationPreference.builder().clinicId(clinicId).build()));
    }

    private Patient patient(Long patientId) {
        return patientRepository.findByIdAndDeletedFalse(patientId).orElse(null);
    }

    private String firstName(Patient p) {
        return p.getFirstName();
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreference p) {
        return NotificationPreferenceResponse.builder()
                .clinicId(p.getClinicId())
                .appointmentBookedSms(p.isAppointmentBookedSms())
                .appointmentBookedEmail(p.isAppointmentBookedEmail())
                .appointmentReminderSms(p.isAppointmentReminderSms())
                .appointmentReminderEmail(p.isAppointmentReminderEmail())
                .appointmentConfirmedSms(p.isAppointmentConfirmedSms())
                .appointmentConfirmedEmail(p.isAppointmentConfirmedEmail())
                .appointmentCancelledSms(p.isAppointmentCancelledSms())
                .appointmentCancelledEmail(p.isAppointmentCancelledEmail())
                .appointmentRescheduledSms(p.isAppointmentRescheduledSms())
                .appointmentRescheduledEmail(p.isAppointmentRescheduledEmail())
                .tokenCalledSms(p.isTokenCalledSms())
                .invoiceIssuedEmail(p.isInvoiceIssuedEmail())
                .paymentReceivedSms(p.isPaymentReceivedSms())
                .paymentReceivedEmail(p.isPaymentReceivedEmail())
                .build();
    }

    private NotificationLogResponse toLogResponse(NotificationLog n) {
        return NotificationLogResponse.builder()
                .id(n.getId())
                .clinicId(n.getClinicId())
                .type(n.getType())
                .channel(n.getChannel())
                .recipient(n.getRecipient())
                .subject(n.getSubject())
                .message(n.getMessage())
                .status(n.getStatus())
                .errorReason(n.getErrorReason())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
