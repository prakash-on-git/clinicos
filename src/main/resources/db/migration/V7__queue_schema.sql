-- V7__queue_schema.sql
-- Phase 7: Queue Management
--
-- Real-life flow:
--   Walk-in patient arrives → receptionist generates token (e.g. Token #7 at Dr. Khan)
--   Appointment patient arrives → receptionist checks them in → token auto-generated
--   Doctor finishes with one patient → calls Token #7 → patient walks to room
--   Consultation done → token marked COMPLETED
--   Patient didn't respond when called → SKIPPED → can be RE-CALLED at end of queue

CREATE TABLE queue_tokens (
    id                  BIGSERIAL       PRIMARY KEY,

    clinic_id           BIGINT          NOT NULL REFERENCES clinics(id),
    doctor_id           BIGINT          NOT NULL REFERENCES doctors(id),
    patient_id          BIGINT          NOT NULL REFERENCES patients(id),

    -- Links to a booked appointment (when patient checks in for appointment).
    -- NULL for walk-ins who have no prior booking.
    -- UNIQUE: one queue token per appointment.
    appointment_id      BIGINT          UNIQUE REFERENCES appointments(id),

    -- Sequential per (clinic, doctor, date). Resets each day starting at 1.
    token_number        INT             NOT NULL,
    queue_date          DATE            NOT NULL,

    -- Status: WAITING | CALLED | IN_PROGRESS | COMPLETED | SKIPPED | CANCELLED
    status              VARCHAR(30)     NOT NULL DEFAULT 'WAITING',

    -- Optional notes from receptionist at check-in (e.g. "Fever, cough")
    notes               TEXT,

    -- Timestamp when this token was called to the doctor's room
    called_at           TIMESTAMP,
    called_by           BIGINT,         -- staff who pressed "Call"

    -- Timestamp when the doctor started seeing this patient
    started_at          TIMESTAMP,

    -- Timestamp when consultation finished
    completed_at        TIMESTAMP,

    -- Who generated this token (receptionist)
    created_by          BIGINT,

    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,

    -- One token number per doctor per day
    CONSTRAINT uq_queue_token UNIQUE (clinic_id, doctor_id, queue_date, token_number)
);

-- Fast lookup: "show all tokens for clinic today"
CREATE INDEX idx_queue_clinic_date ON queue_tokens(clinic_id, queue_date);

-- Doctor-specific queue: "show Dr. Khan's queue today"
CREATE INDEX idx_queue_doctor_date ON queue_tokens(doctor_id, queue_date, token_number);

-- Status filter: "show only WAITING tokens"
CREATE INDEX idx_queue_status ON queue_tokens(clinic_id, queue_date, status);
