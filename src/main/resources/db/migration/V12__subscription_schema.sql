CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    tier VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    max_doctors INT,               -- NULL = unlimited
    max_patients_per_month INT,    -- NULL = unlimited
    price_monthly DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE clinic_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    clinic_id BIGINT NOT NULL UNIQUE REFERENCES clinics(id),
    plan_id BIGINT NOT NULL REFERENCES plans(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','TRIAL','PAST_DUE','CANCELLED')),
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    next_billing_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clinic_subscriptions_clinic_id ON clinic_subscriptions(clinic_id);
CREATE INDEX idx_clinic_subscriptions_status ON clinic_subscriptions(status);

INSERT INTO plans (tier, display_name, max_doctors, max_patients_per_month, price_monthly) VALUES
    ('FREE',       'Free Plan',        1,    50,   0.00),
    ('PRO',        'Pro Plan',         10,   NULL, 2999.00),
    ('ENTERPRISE', 'Enterprise Plan',  NULL, NULL, 9999.00);
