-- ============================================================
-- V10 : Notifications — logs + per-clinic preferences
-- ============================================================

-- ── notification_logs ─────────────────────────────────────────────────────────

CREATE TABLE notification_logs (
    id              BIGSERIAL PRIMARY KEY,

    clinic_id       BIGINT       NOT NULL REFERENCES clinics(id),

    -- What kind of alert was sent
    type            VARCHAR(50)  NOT NULL,

    -- Delivery channel
    channel         VARCHAR(10)  NOT NULL,

    -- Phone number or email address that was targeted
    recipient       VARCHAR(255) NOT NULL,

    subject         VARCHAR(255),   -- used for email
    message         TEXT         NOT NULL,

    status          VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    error_reason    TEXT,

    -- Optional back-reference (appointment_id, invoice_id, queue_token_id, …)
    reference_id    BIGINT,
    reference_type  VARCHAR(50),

    sent_at         TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_notif_channel CHECK (channel IN ('SMS', 'EMAIL')),
    CONSTRAINT chk_notif_status  CHECK (status  IN ('PENDING', 'SENT', 'FAILED', 'SKIPPED'))
);

CREATE INDEX idx_notif_log_clinic       ON notification_logs(clinic_id);
CREATE INDEX idx_notif_log_clinic_type  ON notification_logs(clinic_id, type);
CREATE INDEX idx_notif_log_status       ON notification_logs(clinic_id, status);
CREATE INDEX idx_notif_log_created      ON notification_logs(clinic_id, created_at DESC);

-- ── notification_preferences ──────────────────────────────────────────────────
-- One row per clinic. Boolean flags for each notification type × channel.

CREATE TABLE notification_preferences (
    id                              BIGSERIAL PRIMARY KEY,
    clinic_id                       BIGINT NOT NULL UNIQUE REFERENCES clinics(id),

    -- Appointment booked
    appointment_booked_sms          BOOLEAN NOT NULL DEFAULT TRUE,
    appointment_booked_email        BOOLEAN NOT NULL DEFAULT TRUE,

    -- 24-h reminder (sent by scheduler)
    appointment_reminder_sms        BOOLEAN NOT NULL DEFAULT TRUE,
    appointment_reminder_email      BOOLEAN NOT NULL DEFAULT TRUE,

    -- Appointment confirmed
    appointment_confirmed_sms       BOOLEAN NOT NULL DEFAULT TRUE,
    appointment_confirmed_email     BOOLEAN NOT NULL DEFAULT TRUE,

    -- Cancellation
    appointment_cancelled_sms       BOOLEAN NOT NULL DEFAULT TRUE,
    appointment_cancelled_email     BOOLEAN NOT NULL DEFAULT TRUE,

    -- Reschedule
    appointment_rescheduled_sms     BOOLEAN NOT NULL DEFAULT TRUE,
    appointment_rescheduled_email   BOOLEAN NOT NULL DEFAULT TRUE,

    -- Queue token called
    token_called_sms                BOOLEAN NOT NULL DEFAULT TRUE,

    -- Invoice issued (email only — attaching a PDF link)
    invoice_issued_email            BOOLEAN NOT NULL DEFAULT TRUE,

    -- Payment received
    payment_received_sms            BOOLEAN NOT NULL DEFAULT FALSE,
    payment_received_email          BOOLEAN NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
