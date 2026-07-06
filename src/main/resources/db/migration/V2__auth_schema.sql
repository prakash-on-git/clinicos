-- V2__auth_schema.sql
-- Phase 2: Authentication – roles, users, user_roles, refresh_tokens

-- ── Roles ──────────────────────────────────────────────────────────────────
-- Static lookup table. Seeded below. Not modified at runtime.
CREATE TABLE roles (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── Users ──────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id                              BIGSERIAL    PRIMARY KEY,
    full_name                       VARCHAR(255) NOT NULL,
    email                           VARCHAR(255) NOT NULL UNIQUE,
    password                        VARCHAR(255) NOT NULL,  -- bcrypt hash, never plain text
    email_verified                  BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verify_token              VARCHAR(255),           -- null after verification
    password_reset_token            VARCHAR(255),           -- null when no reset pending
    password_reset_token_expires_at TIMESTAMP,              -- null when no reset pending
    enabled                         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── User ↔ Role (many-to-many) ─────────────────────────────────────────────
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ── Refresh Tokens ─────────────────────────────────────────────────────────
-- One row per active session. Deleted on logout or token rotation.
CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── Indexes ────────────────────────────────────────────────────────────────
CREATE INDEX idx_users_email              ON users(email);
CREATE INDEX idx_refresh_tokens_token     ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id   ON refresh_tokens(user_id);

-- ── Seed default roles ─────────────────────────────────────────────────────
-- These are inserted here so every environment (dev, CI, prod) has them
-- from first startup, with zero manual steps.
INSERT INTO roles (name) VALUES
    ('SUPER_ADMIN'),
    ('CLINIC_ADMIN'),
    ('DOCTOR'),
    ('RECEPTIONIST');
