-- V4__doctor_schema.sql
-- Phase 4: Doctor Module
-- Tables: doctors, doctor_weekly_schedule, doctor_breaks,
--         doctor_day_overrides, doctor_leave_dates,
--         treatment_types, doctor_treatments

-- ── Doctors ──────────────────────────────────────────────────────────────────
-- A doctor profile does NOT require a user account (admin enters manually first;
-- the user account can be linked later via POST /doctors/{id}/link-user).
-- A doctor belongs to exactly one clinic (multi-clinic support = future phase).
CREATE TABLE doctors (
    id                  BIGSERIAL       PRIMARY KEY,
    clinic_id           BIGINT          NOT NULL REFERENCES clinics(id),

    -- Optional link to a users row (DOCTOR role user).
    -- NULL = doctor has no app login yet.
    -- UNIQUE = one user account maps to at most one doctor profile.
    user_id             BIGINT          UNIQUE REFERENCES users(id) ON DELETE SET NULL,

    full_name           VARCHAR(255)    NOT NULL,

    -- URL-safe slug, auto-generated from full_name. Unique globally.
    slug                VARCHAR(100)    NOT NULL UNIQUE,

    email               VARCHAR(255),
    phone               VARCHAR(30),
    qualification       VARCHAR(255),   -- e.g. "MBBS, MD (Cardiology)"
    specialization      VARCHAR(255),   -- e.g. "Cardiologist"
    bio                 TEXT,
    avatar_url          VARCHAR(512),
    registration_number VARCHAR(100),   -- medical council registration number

    -- Default consultation fee (can be overridden per treatment)
    consultation_fee    NUMERIC(10, 2),

    -- is_active = FALSE: temporarily not seeing patients (sabbatical, on boarding, etc.)
    -- Different from soft-delete: active state can be toggled; delete is permanent.
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Soft delete
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          BIGINT          REFERENCES users(id),

    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doctors_clinic_id ON doctors(clinic_id);
CREATE UNIQUE INDEX idx_doctors_slug ON doctors(slug);
-- Partial index: only non-deleted doctors (boolean constant, always IMMUTABLE)
CREATE INDEX idx_doctors_clinic_active ON doctors(clinic_id, is_active) WHERE is_deleted = FALSE;


-- ── Doctor Weekly Schedule ────────────────────────────────────────────────────
-- Defines when a doctor is available each week. One row = one contiguous working
-- window on one day. Multiple rows on the same day = split shifts (e.g. 9am–1pm
-- and 5pm–9pm). A day with no rows = doctor doesn't work that day.
--
-- Times are in the clinic's local timezone (same as clinic_business_hours).
-- Must be within clinic's open hours (validated in service layer, not DB).
CREATE TABLE doctor_weekly_schedule (
    id          BIGSERIAL   PRIMARY KEY,
    doctor_id   BIGINT      NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL
                CHECK (day_of_week IN
                    ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    shift_label VARCHAR(50),            -- optional: "Morning OPD", "Evening OPD"
    CONSTRAINT chk_doctor_shift_times CHECK (end_time > start_time),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doctor_schedule_doctor_day ON doctor_weekly_schedule(doctor_id, day_of_week);


-- ── Doctor Breaks ─────────────────────────────────────────────────────────────
-- Recurring intra-day breaks that are SUBTRACTED from the working window during
-- slot generation. Stored per day-of-week (recurring weekly).
-- Examples: Lunch 13:00–14:00 every Mon–Fri, Zuhr prayer 13:20–13:50 daily.
--
-- One-time breaks (e.g. "tea break on 2026-07-10 only") are handled via
-- CUSTOM_HOURS override in doctor_day_overrides — admin sets adjusted schedule
-- for that day rather than adding a one-time break here.
--
-- Break times must align to 10-minute boundaries (validated in service layer)
-- so they don't create partial slots.
CREATE TABLE doctor_breaks (
    id          BIGSERIAL   PRIMARY KEY,
    doctor_id   BIGINT      NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL
                CHECK (day_of_week IN
                    ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    break_start TIME        NOT NULL,
    break_end   TIME        NOT NULL,
    break_type  VARCHAR(20) NOT NULL DEFAULT 'BREAK'
                CHECK (break_type IN ('LUNCH','TEA','PRAYER','PERSONAL','BREAK')),
    label       VARCHAR(100),          -- optional: "Lunch Break", "Zuhr Prayer"
    CONSTRAINT chk_doctor_break_times CHECK (break_end > break_start),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doctor_breaks_doctor_day ON doctor_breaks(doctor_id, day_of_week);


-- ── Doctor Day Overrides ──────────────────────────────────────────────────────
-- Per-date deviations from the weekly schedule. One row per date per doctor.
-- Four override types:
--
--   DAY_OFF      → doctor not available this date at all (start_time/end_time NULL).
--                   Used for: single-day sick leave, public duty, training, etc.
--                   Different from doctor_leave_dates (see below).
--
--   LATE_START   → start_time = new effective start (later than schedule start).
--                   All appointment slots that day begin from this new start.
--
--   EARLY_END    → end_time = new effective end (earlier than schedule end).
--                   Slots are truncated; last available slot ends at end_time.
--
--   CUSTOM_HOURS → both start_time and end_time are set.
--                   Completely replaces the weekly schedule for this date.
--                   Use when doctor works unusual hours (e.g. half-day, different shift).
--
-- A LATE_START + EARLY_END on the same date → use CUSTOM_HOURS with both times.
-- One row per (doctor, date) enforced by UNIQUE constraint.
CREATE TABLE doctor_day_overrides (
    id              BIGSERIAL   PRIMARY KEY,
    doctor_id       BIGINT      NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    override_date   DATE        NOT NULL,
    override_type   VARCHAR(20) NOT NULL
                    CHECK (override_type IN ('DAY_OFF','LATE_START','EARLY_END','CUSTOM_HOURS')),
    start_time      TIME,   -- LATE_START / CUSTOM_HOURS
    end_time        TIME,   -- EARLY_END  / CUSTOM_HOURS
    reason          VARCHAR(500),
    created_by      BIGINT  REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_doctor_date_override UNIQUE (doctor_id, override_date)
);

CREATE INDEX idx_doctor_overrides_doctor_date ON doctor_day_overrides(doctor_id, override_date);


-- ── Doctor Leave Dates ────────────────────────────────────────────────────────
-- Full-day absence. One row per calendar date.
-- Design: one row per date (not range) mirrors clinic_closure_dates and enables a
-- simple EXISTS query in slot generation instead of range-overlap arithmetic.
--
-- Why separate from DAY_OFF override?
--   DAY_OFF = operational/unplanned (doctor calls in sick this morning → admin records override).
--   Leave = planned, HR-tracked, potentially multi-day (booked a week in advance).
--   Different UI flows, different query patterns (leave reports, leave balance tracking).
CREATE TABLE doctor_leave_dates (
    id          BIGSERIAL   PRIMARY KEY,
    doctor_id   BIGINT      NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    leave_date  DATE        NOT NULL,
    leave_type  VARCHAR(20) NOT NULL DEFAULT 'OTHER'
                CHECK (leave_type IN ('SICK','VACATION','CONFERENCE','EMERGENCY','PERSONAL','OTHER')),
    reason      VARCHAR(500),
    created_by  BIGINT      REFERENCES users(id),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_doctor_leave_date UNIQUE (doctor_id, leave_date)
);

CREATE INDEX idx_doctor_leave_doctor_date ON doctor_leave_dates(doctor_id, leave_date);


-- ── Treatment Types ───────────────────────────────────────────────────────────
-- Clinic-level catalog of treatments.
-- All durations are multiples of 10 minutes (enforced by CHECK constraint)
-- so they always occupy whole 10-minute slot boundaries.
-- Color coding lets the front-end render appointments in different colours per type.
CREATE TABLE treatment_types (
    id                    BIGSERIAL       PRIMARY KEY,
    clinic_id             BIGINT          NOT NULL REFERENCES clinics(id) ON DELETE CASCADE,
    name                  VARCHAR(255)    NOT NULL,
    description           TEXT,

    -- Duration in minutes. Must be >= 10 and a multiple of 10.
    default_duration_mins INTEGER         NOT NULL
                          CHECK (default_duration_mins >= 10
                             AND default_duration_mins % 10 = 0),

    default_fee           NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    color_hex             VARCHAR(7),     -- UI color e.g. "#3B82F6"

    is_active             BOOLEAN         NOT NULL DEFAULT TRUE,
    is_deleted            BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMP,
    deleted_by            BIGINT          REFERENCES users(id),

    created_at            TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_treatment_clinic_name UNIQUE (clinic_id, name)
);

CREATE INDEX idx_treatment_types_clinic ON treatment_types(clinic_id) WHERE is_deleted = FALSE;


-- ── Doctor Treatments ─────────────────────────────────────────────────────────
-- Junction table: which treatments a doctor offers at this clinic,
-- with optional per-doctor fee and duration overrides.
-- Effective duration = COALESCE(custom_duration_mins, treatment_types.default_duration_mins)
-- Effective fee      = COALESCE(custom_fee, treatment_types.default_fee)
CREATE TABLE doctor_treatments (
    id                  BIGSERIAL       PRIMARY KEY,
    doctor_id           BIGINT          NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    treatment_type_id   BIGINT          NOT NULL REFERENCES treatment_types(id) ON DELETE CASCADE,

    -- Override per-doctor duration (must be multiple of 10 if provided)
    custom_duration_mins INTEGER
                         CHECK (custom_duration_mins IS NULL
                             OR (custom_duration_mins >= 10
                             AND custom_duration_mins % 10 = 0)),

    -- Override per-doctor fee
    custom_fee          NUMERIC(10, 2),

    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,

    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_doctor_treatment UNIQUE (doctor_id, treatment_type_id)
);

CREATE INDEX idx_doctor_treatments_doctor ON doctor_treatments(doctor_id) WHERE is_active = TRUE;
