package com.prakash.clinicos.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false, unique = true)
    private Long clinicId;

    @Builder.Default @Column(name = "appointment_booked_sms")       private boolean appointmentBookedSms = true;
    @Builder.Default @Column(name = "appointment_booked_email")     private boolean appointmentBookedEmail = true;
    @Builder.Default @Column(name = "appointment_reminder_sms")     private boolean appointmentReminderSms = true;
    @Builder.Default @Column(name = "appointment_reminder_email")   private boolean appointmentReminderEmail = true;
    @Builder.Default @Column(name = "appointment_confirmed_sms")    private boolean appointmentConfirmedSms = true;
    @Builder.Default @Column(name = "appointment_confirmed_email")  private boolean appointmentConfirmedEmail = true;
    @Builder.Default @Column(name = "appointment_cancelled_sms")    private boolean appointmentCancelledSms = true;
    @Builder.Default @Column(name = "appointment_cancelled_email")  private boolean appointmentCancelledEmail = true;
    @Builder.Default @Column(name = "appointment_rescheduled_sms")  private boolean appointmentRescheduledSms = true;
    @Builder.Default @Column(name = "appointment_rescheduled_email") private boolean appointmentRescheduledEmail = true;
    @Builder.Default @Column(name = "token_called_sms")             private boolean tokenCalledSms = true;
    @Builder.Default @Column(name = "invoice_issued_email")         private boolean invoiceIssuedEmail = true;
    @Builder.Default @Column(name = "payment_received_sms")         private boolean paymentReceivedSms = false;
    @Builder.Default @Column(name = "payment_received_email")       private boolean paymentReceivedEmail = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
