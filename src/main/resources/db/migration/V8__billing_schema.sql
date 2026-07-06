-- ============================================================
-- V8 : Billing — invoices, invoice_items, payments
-- ============================================================

-- ── invoices ─────────────────────────────────────────────────────────────────

CREATE TABLE invoices (
    id                  BIGSERIAL PRIMARY KEY,

    clinic_id           BIGINT       NOT NULL REFERENCES clinics(id),
    patient_id          BIGINT       NOT NULL REFERENCES patients(id),
    doctor_id           BIGINT       NOT NULL REFERENCES doctors(id),

    -- Nullable: invoice may be created for a walk-in without a formal appointment
    appointment_id      BIGINT       REFERENCES appointments(id),

    -- Human-readable invoice number, unique per clinic: INV-2026-00001
    invoice_number      VARCHAR(30)  NOT NULL,

    invoice_date        DATE         NOT NULL DEFAULT CURRENT_DATE,
    due_date            DATE,

    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',

    -- Monetary breakdown (all in smallest unit readable as decimal, e.g. 1250.00)
    subtotal            NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_percent    NUMERIC(5,2)  NOT NULL DEFAULT 0
                            CONSTRAINT chk_discount_pct CHECK (discount_percent >= 0 AND discount_percent <= 100),
    discount_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax_percent         NUMERIC(5,2)  NOT NULL DEFAULT 0
                            CONSTRAINT chk_tax_pct CHECK (tax_percent >= 0),
    tax_amount          NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(12,2) NOT NULL DEFAULT 0,
    amount_paid         NUMERIC(12,2) NOT NULL DEFAULT 0,
    amount_due          NUMERIC(12,2) NOT NULL DEFAULT 0,

    notes               TEXT,

    -- Workflow audit
    issued_by           BIGINT,
    issued_at           TIMESTAMP,
    cancelled_by        BIGINT,
    cancelled_at        TIMESTAMP,
    cancellation_reason TEXT,

    created_by          BIGINT,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_invoice_number_clinic UNIQUE (clinic_id, invoice_number),
    CONSTRAINT uq_invoice_appointment   UNIQUE (appointment_id),
    CONSTRAINT chk_invoice_status CHECK (status IN
        ('DRAFT','ISSUED','PAID','PARTIALLY_PAID','CANCELLED','REFUNDED'))
);

CREATE INDEX idx_invoice_clinic        ON invoices(clinic_id);
CREATE INDEX idx_invoice_patient       ON invoices(patient_id);
CREATE INDEX idx_invoice_doctor        ON invoices(doctor_id);
CREATE INDEX idx_invoice_clinic_date   ON invoices(clinic_id, invoice_date);
CREATE INDEX idx_invoice_status        ON invoices(clinic_id, status);

-- ── invoice_items ─────────────────────────────────────────────────────────────

CREATE TABLE invoice_items (
    id                  BIGSERIAL PRIMARY KEY,

    invoice_id          BIGINT        NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,

    -- Optional back-reference for reporting; service may pass null for ad-hoc charges
    treatment_type_id   BIGINT,

    description         VARCHAR(255)  NOT NULL,
    quantity            NUMERIC(8,2)  NOT NULL DEFAULT 1
                            CONSTRAINT chk_item_qty CHECK (quantity > 0),
    unit_price          NUMERIC(12,2) NOT NULL
                            CONSTRAINT chk_item_unit_price CHECK (unit_price >= 0),
    total_price         NUMERIC(12,2) NOT NULL,

    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoice_items_invoice ON invoice_items(invoice_id);

-- ── payments ─────────────────────────────────────────────────────────────────

CREATE TABLE payments (
    id                    BIGSERIAL PRIMARY KEY,

    invoice_id            BIGINT        NOT NULL REFERENCES invoices(id),
    clinic_id             BIGINT        NOT NULL REFERENCES clinics(id),

    amount                NUMERIC(12,2) NOT NULL
                              CONSTRAINT chk_payment_amount CHECK (amount > 0),
    payment_method        VARCHAR(20)   NOT NULL,
    payment_date          DATE          NOT NULL DEFAULT CURRENT_DATE,

    -- External reference: UPI txn ID, card auth code, etc.
    transaction_reference VARCHAR(100),
    notes                 TEXT,

    received_by           BIGINT,

    created_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_payment_method CHECK (payment_method IN
        ('CASH','CARD','UPI','NET_BANKING','INSURANCE','OTHER'))
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_clinic  ON payments(clinic_id);
