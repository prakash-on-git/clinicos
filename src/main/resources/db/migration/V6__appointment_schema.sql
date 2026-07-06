-- V6__appointment_schema.sql
-- Phase 6: Appointment System

-- Appointment status values:
--   PENDING       – just booked, awaiting confirmation
--   CONFIRMED     – confirmed by receptionist / clinic admin
--   IN_PROGRESS   – doctor is currently seeing this patient (useful for queue)
--   COMPLETED     – consultation finished
--   CANCELLED     – cancelled by patient or staff; slot is freed
--   NO_SHOW       – patient didn't arrive; slot is freed
--   RESCHEDULED   – this record was moved to a new time;
--                   a new appointment row was created pointing back here

CREATE TABLE appointments (
    id                      BIGSERIAL       PRIMARY KEY,

    -- Clinic, doctor, patient (plain FKs — no cross-module entity coupling)
    clinic_id               BIGINT          NOT NULL REFERENCES clinics(id),
    doctor_id               BIGINT          NOT NULL REFERENCES doctors(id),
    patient_id              BIGINT          NOT NULL REFERENCES patients(id),

    -- Optional: which treatment type was booked
    -- Determines slot duration. Null = 10-minute default slot.
    treatment_type_id       BIGINT          REFERENCES treatment_types(id),

    -- When
    appointment_date        DATE            NOT NULL,
    start_time              TIME            NOT NULL,
    end_time                TIME            NOT NULL,   -- start_time + duration_mins

    -- Snapshot of duration at booking time.
    -- Treatment default durations can change later; appointments must not be affected.
    duration_mins           INT             NOT NULL
        CHECK (duration_mins >= 10 AND duration_mins % 10 = 0),

    -- Lifecycle
    status                  VARCHAR(30)     NOT NULL DEFAULT 'PENDING',

    -- Patient's stated reason for the visit / chief complaint
    reason                  TEXT,

    -- Internal notes added during or after the consultation
    notes                   TEXT,

    -- Cancellation tracking
    cancelled_by            BIGINT,
    cancelled_at            TIMESTAMP,
    cancellation_reason     TEXT,

    -- Confirmation tracking
    confirmed_by            BIGINT,
    confirmed_at            TIMESTAMP,

    -- Reschedule chain: the NEW appointment stores the ID of the OLD one.
    -- Allows tracing the full reschedule history.
    rescheduled_from_id     BIGINT          REFERENCES appointments(id),

    -- Who booked this appointment (staff member or patient user)
    booked_by               BIGINT,

    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL,

    -- Core uniqueness: a doctor cannot have two active appointments at the same start time.
    -- CANCELLED / RESCHEDULED / NO_SHOW appointments do not block the slot —
    -- that logic is enforced in the service layer.
    CONSTRAINT uq_appointment_doctor_date_time UNIQUE (doctor_id, appointment_date, start_time)
);

-- Most common query: "show me all appointments for this clinic today"
CREATE INDEX idx_appt_clinic_date   ON appointments(clinic_id, appointment_date);

-- Doctor's schedule: "what appointments does Dr. X have on date Y?"
CREATE INDEX idx_appt_doctor_date   ON appointments(doctor_id, appointment_date);

-- Patient history: "all appointments for patient P"
CREATE INDEX idx_appt_patient       ON appointments(patient_id, appointment_date DESC);

-- Status filter: "show all PENDING appointments for this clinic"
CREATE INDEX idx_appt_clinic_status ON appointments(clinic_id, status);
