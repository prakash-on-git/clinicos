-- V11__patient_portal_schema.sql
-- Phase 12: Patient Portal — PATIENT role + patient notification opt-out columns

-- ── PATIENT role ─────────────────────────────────────────────────────────────
-- Added to the existing roles lookup table so patients can log in and view
-- their own data without having clinic-admin access.
INSERT INTO roles (name) VALUES ('PATIENT');

-- ── Patient notification opt-out ─────────────────────────────────────────────
-- Patients can opt out of SMS and/or email notifications independently.
-- These flags are checked by NotificationService before dispatching.
-- Default: opted IN (false = not opted out = notifications enabled).
ALTER TABLE patients
    ADD COLUMN sms_opt_out   BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_opt_out BOOLEAN NOT NULL DEFAULT FALSE;
