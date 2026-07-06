-- V13__audit_log_schema.sql
-- Phase 14: Audit Logs — immutable record of every clinical data mutation

CREATE TABLE audit_logs (
    id           BIGSERIAL    PRIMARY KEY,
    clinic_id    BIGINT       REFERENCES clinics(id),          -- null = platform-level action
    entity_type  VARCHAR(50)  NOT NULL,                        -- PATIENT, DOCTOR, APPOINTMENT, etc.
    entity_id    BIGINT,
    action       VARCHAR(20)  NOT NULL
                     CHECK (action IN ('CREATE','UPDATE','DELETE')),
    changed_by   BIGINT       REFERENCES users(id),
    before_state TEXT,                                         -- JSON snapshot before change (null for CREATE)
    after_state  TEXT,                                         -- JSON snapshot after change (null for DELETE)
    ip_address   VARCHAR(45),                                  -- IPv4 or IPv6
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()           -- immutable — no updated_at
);

-- Queries filtered by clinic (most common)
CREATE INDEX idx_audit_logs_clinic_id  ON audit_logs(clinic_id);
-- Queries for a specific entity's full history
CREATE INDEX idx_audit_logs_entity     ON audit_logs(entity_type, entity_id);
-- Queries by who made the change
CREATE INDEX idx_audit_logs_changed_by ON audit_logs(changed_by);
-- Time-range queries
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
