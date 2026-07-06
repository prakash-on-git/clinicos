-- V5__patient_schema.sql
-- Phase 5: Patient Module
-- Table: patients

-- ── Patients ─────────────────────────────────────────────────────────────────
-- Patients are clinic-scoped: the same person visiting two clinics will have
-- two separate records (one per clinic). This keeps multi-tenant data clean.
--
-- A patient may optionally be linked to a user account (self-service portal).
-- Without a user account the clinic staff manage everything on the patient's behalf.
CREATE TABLE patients (
    id                          BIGSERIAL       PRIMARY KEY,

    -- Every patient belongs to exactly one clinic.
    clinic_id                   BIGINT          NOT NULL REFERENCES clinics(id),

    -- Optional link to a users row (PATIENT role user).
    -- NULL = patient has no self-service login yet.
    -- UNIQUE = one user account maps to at most one patient profile.
    user_id                     BIGINT          UNIQUE REFERENCES users(id) ON DELETE SET NULL,

    -- Core identity
    first_name                  VARCHAR(100)    NOT NULL,
    last_name                   VARCHAR(100),

    -- Phone is required and unique per clinic.
    -- Receptionists look up patients by mobile number all the time.
    phone                       VARCHAR(30)     NOT NULL,

    email                       VARCHAR(255),
    date_of_birth               DATE,

    -- Gender stored as text so we don't need a DB enum type.
    -- Application enforces: MALE | FEMALE | OTHER | PREFER_NOT_TO_SAY
    gender                      VARCHAR(30),

    -- Medical background (free text — structured data for prescriptions is Phase 7)
    blood_group                 VARCHAR(10),        -- e.g. A+, O-, AB+
    allergies                   TEXT,               -- "Penicillin, Aspirin, Shellfish"
    chronic_conditions          TEXT,               -- "Diabetes Type 2, Hypertension"
    current_medications         TEXT,               -- "Metformin 500mg, Amlodipine 5mg"

    -- Emergency contact (important for clinics treating elderly/children)
    emergency_contact_name      VARCHAR(255),
    emergency_contact_phone     VARCHAR(30),
    emergency_contact_relation  VARCHAR(100),       -- "Mother", "Spouse", "Guardian"

    -- Address (free text for now; can be structured later)
    address                     TEXT,

    -- Staff notes (internal; not visible to patient in self-service)
    notes                       TEXT,

    -- Soft delete + lifecycle tracking
    is_active                   BOOLEAN         NOT NULL DEFAULT TRUE,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP,
    deleted_by                  BIGINT,

    -- Which staff member registered this patient
    created_by                  BIGINT,

    created_at                  TIMESTAMP       NOT NULL,
    updated_at                  TIMESTAMP       NOT NULL,

    -- One patient record per phone number per clinic.
    -- Prevents duplicate registrations of the same person.
    CONSTRAINT uq_patient_clinic_phone UNIQUE (clinic_id, phone)
);

-- Fast lookup by clinic (most common query: "all patients for this clinic")
CREATE INDEX idx_patients_clinic ON patients(clinic_id) WHERE is_deleted = FALSE;

-- Fast lookup by phone within a clinic (receptionist "find patient" workflow)
CREATE INDEX idx_patients_clinic_phone ON patients(clinic_id, phone);

-- Support search by first/last name (used with LOWER() in JPQL)
CREATE INDEX idx_patients_name ON patients(clinic_id, first_name, last_name);

-- Support self-view: find patient profile by linked user account
CREATE INDEX idx_patients_user ON patients(user_id) WHERE user_id IS NOT NULL;
