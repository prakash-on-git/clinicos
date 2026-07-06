-- V3__clinic_schema.sql
-- Phase 3: Clinic Module
-- Tables: clinics, clinic_business_hours, clinic_closure_dates, clinic_settings
-- Also: ALTER TABLE users ADD COLUMN clinic_id (links CLINIC_ADMIN to their clinic)

-- ── Clinics ─────────────────────────────────────────────────────────────────
CREATE TABLE clinics (
    id                      BIGSERIAL       PRIMARY KEY,

    -- Identity & branding
    name                    VARCHAR(255)    NOT NULL,
    slug                    VARCHAR(100)    NOT NULL UNIQUE,    -- URL-safe, e.g. "sunrise-clinic"
    description             TEXT,
    logo_url                VARCHAR(512),

    -- Contact
    phone                   VARCHAR(30),
    email                   VARCHAR(255),
    website                 VARCHAR(512),

    -- Address
    address_line1           VARCHAR(255),
    address_line2           VARCHAR(255),
    city                    VARCHAR(100),
    state                   VARCHAR(100),
    postal_code             VARCHAR(20),
    country                 VARCHAR(100)    NOT NULL DEFAULT 'India',

    -- Timezone in IANA format (e.g. 'Asia/Kolkata', 'America/New_York').
    -- All business hours are stored as local clinic time and interpreted using this timezone.
    -- IANA IDs handle daylight saving correctly; never store '+05:30' style offsets.
    timezone                VARCHAR(100)    NOT NULL DEFAULT 'Asia/Kolkata',

    -- Ownership: the CLINIC_ADMIN user who created and manages this clinic.
    -- Stored as a plain FK, not mapped as @ManyToOne, to avoid cross-module coupling.
    owner_user_id           BIGINT          NOT NULL REFERENCES users(id),

    -- ── Open mode ────────────────────────────────────────────────────────────
    -- When TRUE: clinic is always open; clinic_business_hours rows are ignored.
    -- CLINIC_ADMIN can still set hours (for future use when 24/7 is turned off).
    is_24_7                 BOOLEAN         NOT NULL DEFAULT FALSE,

    -- ── Emergency close ──────────────────────────────────────────────────────
    -- Temporary suspension WITHOUT soft-deleting the clinic.
    -- Use case: power outage, staff absence, doctor emergency.
    -- Survives server restarts. Reversal: POST /emergency-reopen.
    is_emergency_closed     BOOLEAN         NOT NULL DEFAULT FALSE,
    emergency_close_reason  VARCHAR(500),
    emergency_closed_at     TIMESTAMP,
    emergency_closed_by     BIGINT          REFERENCES users(id),

    -- ── Soft delete ───────────────────────────────────────────────────────────
    -- Permanent deactivation. Separate from emergency close.
    -- Once deleted, cannot be restored via API (data is preserved for audit).
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP,
    deleted_by              BIGINT          REFERENCES users(id),

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clinics_owner_user_id  ON clinics(owner_user_id);
CREATE INDEX idx_clinics_slug           ON clinics(slug);
CREATE INDEX idx_clinics_active         ON clinics(is_deleted) WHERE is_deleted = FALSE;

-- ── Business Hours ────────────────────────────────────────────────────────────
-- One row = one contiguous shift on one day of the week.
-- Multi-shift day (morning + evening) = two rows for the same day.
-- A day with no shifts = no rows for that day = clinic is closed that day.
--
-- Design choice: shift-per-row (normalized) vs JSON array on one row.
-- Reason: shift-per-row allows SQL-level CHECK constraints on time order,
-- easy deletion of individual shifts, and clean overlap detection.
CREATE TABLE clinic_business_hours (
    id           BIGSERIAL   PRIMARY KEY,
    clinic_id    BIGINT      NOT NULL REFERENCES clinics(id) ON DELETE CASCADE,

    -- Stored as Java DayOfWeek enum name (MONDAY, TUESDAY, ..., SUNDAY).
    -- Using VARCHAR + CHECK instead of PostgreSQL ENUM so Flyway migrations stay simple.
    day_of_week  VARCHAR(10) NOT NULL
                 CHECK (day_of_week IN
                     ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),

    -- Local clinic time — interpreted in the timezone stored on the clinic row.
    -- Java type: LocalTime → PostgreSQL TIME WITHOUT TIME ZONE (no tz, intentional).
    open_time    TIME        NOT NULL,
    close_time   TIME        NOT NULL,

    -- Optional human-readable label for multi-shift days.
    -- Examples: 'Morning', 'Evening', 'Night', 'Lunch Session'
    shift_label  VARCHAR(50),

    -- Enforce close > open at DB level as a safety net.
    -- Overlap detection between shifts is done in the service layer (SQL can't express that).
    -- Overnight shifts (22:00–02:00 crossing midnight) are out of scope — model doesn't support them.
    CONSTRAINT chk_close_after_open CHECK (close_time > open_time),

    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_biz_hours_clinic_day ON clinic_business_hours(clinic_id, day_of_week);

-- ── Clinic Closure Dates ──────────────────────────────────────────────────────
-- Future-dated (or past) holiday / planned closure dates.
-- "Clinic is closed on 2026-08-15 for Independence Day" → one row.
-- NOT for emergency close (that flag is on the clinic row itself).
CREATE TABLE clinic_closure_dates (
    id           BIGSERIAL   PRIMARY KEY,
    clinic_id    BIGINT      NOT NULL REFERENCES clinics(id) ON DELETE CASCADE,

    -- Date in clinic's local timezone (DATE, not TIMESTAMP).
    -- Java type: LocalDate → PostgreSQL DATE.
    closure_date DATE        NOT NULL,

    -- Human-readable reason shown in UI and notifications.
    reason       VARCHAR(255),

    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),

    -- One closure entry per day per clinic.
    CONSTRAINT uq_clinic_closure_date UNIQUE (clinic_id, closure_date)
);

-- Composite index on (clinic_id, closure_date) for efficient future-date queries.
-- Note: a partial index WHERE closure_date >= CURRENT_DATE is not allowed in PostgreSQL
-- because CURRENT_DATE is STABLE (not IMMUTABLE). The full composite index is sufficient.
CREATE INDEX idx_closure_clinic_date ON clinic_closure_dates(clinic_id, closure_date);

-- ── Clinic Settings ───────────────────────────────────────────────────────────
-- One-to-one with clinics. Extracted into its own table so the clinics table
-- stays focused on identity and location (single-responsibility principle).
-- Auto-created with defaults when a clinic is created.
CREATE TABLE clinic_settings (
    id                          BIGSERIAL   PRIMARY KEY,
    clinic_id                   BIGINT      NOT NULL UNIQUE REFERENCES clinics(id) ON DELETE CASCADE,

    -- How many minutes each appointment slot lasts. Min 5, max 240 (4 hours).
    appointment_duration_mins   INTEGER     NOT NULL DEFAULT 20
                                CHECK (appointment_duration_mins BETWEEN 5 AND 240),

    -- How many days in advance a patient can book. e.g. 30 → book up to 30 days ahead.
    advance_booking_days        INTEGER     NOT NULL DEFAULT 30
                                CHECK (advance_booking_days BETWEEN 1 AND 365),

    -- How many hours before appointment start the cancellation is penalty-free. 0 = no window.
    cancellation_window_hours   INTEGER     NOT NULL DEFAULT 24
                                CHECK (cancellation_window_hours BETWEEN 0 AND 168),

    -- 0 means unlimited. Cap per day regardless of slot count.
    max_patients_per_day        INTEGER     NOT NULL DEFAULT 0
                                CHECK (max_patients_per_day >= 0),

    -- Whether walk-in patients (without an appointment) are accepted.
    allow_walk_ins              BOOLEAN     NOT NULL DEFAULT TRUE,

    -- TRUE  → booking is auto-confirmed immediately.
    -- FALSE → booking stays 'PENDING' until receptionist manually confirms.
    auto_confirm_appointments   BOOLEAN     NOT NULL DEFAULT TRUE,

    created_at                  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── Link user to their clinic ─────────────────────────────────────────────────
-- A CLINIC_ADMIN can own exactly one clinic.
-- NULL = they haven't created one yet.
-- Set when clinic is created; used by GET /clinics/mine to avoid a full table scan.
-- ON DELETE SET NULL: if the clinic is hard-deleted from DB, user is unlinked (edge case).
ALTER TABLE users ADD COLUMN clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL;
CREATE INDEX idx_users_clinic_id ON users(clinic_id);
