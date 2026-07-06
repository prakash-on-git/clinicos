-- ============================================================
-- V9 : Medical Records — prescriptions, vitals, clinical_notes
-- ============================================================

-- ── prescriptions ─────────────────────────────────────────────────────────────

CREATE TABLE prescriptions (
    id              BIGSERIAL PRIMARY KEY,

    clinic_id       BIGINT  NOT NULL REFERENCES clinics(id),
    doctor_id       BIGINT  NOT NULL REFERENCES doctors(id),
    patient_id      BIGINT  NOT NULL REFERENCES patients(id),

    -- One prescription per appointment; nullable for walk-in prescriptions
    appointment_id  BIGINT  UNIQUE REFERENCES appointments(id),

    diagnosis       TEXT,
    instructions    TEXT,
    follow_up_date  DATE,

    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rx_clinic    ON prescriptions(clinic_id);
CREATE INDEX idx_rx_patient   ON prescriptions(patient_id);
CREATE INDEX idx_rx_doctor    ON prescriptions(doctor_id);

-- ── prescription_medicines ────────────────────────────────────────────────────

CREATE TABLE prescription_medicines (
    id              BIGSERIAL PRIMARY KEY,

    prescription_id BIGINT       NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,

    medicine_name   VARCHAR(255) NOT NULL,
    dosage          VARCHAR(100) NOT NULL,   -- e.g. "5mg", "10ml"
    frequency       VARCHAR(100) NOT NULL,   -- e.g. "Once daily", "Twice daily after meals"
    duration_days   INT,
    route           VARCHAR(50),             -- e.g. "Oral", "Topical", "IV"
    notes           TEXT,

    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rx_medicine_prescription ON prescription_medicines(prescription_id);

-- ── vitals ────────────────────────────────────────────────────────────────────

CREATE TABLE vitals (
    id                  BIGSERIAL PRIMARY KEY,

    clinic_id           BIGINT NOT NULL REFERENCES clinics(id),
    patient_id          BIGINT NOT NULL REFERENCES patients(id),

    -- One vitals record per appointment
    appointment_id      BIGINT UNIQUE REFERENCES appointments(id),

    -- Blood pressure
    systolic_bp         INT,    -- mmHg
    diastolic_bp        INT,    -- mmHg

    pulse_bpm           INT,
    temperature_celsius NUMERIC(4,1),
    weight_kg           NUMERIC(6,2),
    height_cm           NUMERIC(6,2),
    spo2_percent        INT,

    notes               TEXT,

    recorded_by         BIGINT,
    recorded_at         TIMESTAMP NOT NULL DEFAULT NOW(),

    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vitals_clinic   ON vitals(clinic_id);
CREATE INDEX idx_vitals_patient  ON vitals(patient_id);

-- ── clinical_notes ────────────────────────────────────────────────────────────

CREATE TABLE clinical_notes (
    id              BIGSERIAL PRIMARY KEY,

    clinic_id       BIGINT NOT NULL REFERENCES clinics(id),
    doctor_id       BIGINT NOT NULL REFERENCES doctors(id),
    patient_id      BIGINT NOT NULL REFERENCES patients(id),

    -- One note per appointment
    appointment_id  BIGINT UNIQUE REFERENCES appointments(id),

    -- SOAP format — any field may be null (doctor may not use all sections)
    subjective      TEXT,   -- patient's reported symptoms / chief complaint
    objective       TEXT,   -- examination findings, test results
    assessment      TEXT,   -- diagnosis / differential
    plan            TEXT,   -- treatment plan, referrals, follow-up

    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notes_clinic   ON clinical_notes(clinic_id);
CREATE INDEX idx_notes_patient  ON clinical_notes(patient_id);
