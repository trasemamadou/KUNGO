-- ============================================================================
-- V6: Paiements, commissions, reversements, factures
-- ============================================================================

CREATE TABLE payments
(
    id                    UUID PRIMARY KEY,
    booking_id            UUID          NOT NULL REFERENCES bookings (id),
    provider              VARCHAR(20)   NOT NULL,
    amount                NUMERIC(12, 2),
    currency              VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    status                VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    transaction_ref       VARCHAR(255),
    provider_client_secret TEXT,
    invoice_url           TEXT,
    paid_at               TIMESTAMP,
    created_at            TIMESTAMP     NOT NULL,
    updated_at            TIMESTAMP     NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    deleted               BOOLEAN       NOT NULL DEFAULT FALSE,
    version               BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_payments_booking UNIQUE (booking_id),
    CONSTRAINT uq_payments_transaction_ref UNIQUE (transaction_ref)
);

CREATE INDEX idx_payments_status ON payments (status);

CREATE TABLE commissions
(
    id                  UUID PRIMARY KEY,
    payment_id          UUID          NOT NULL REFERENCES payments (id),
    percentage_applied  NUMERIC(5, 2) NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    created_at          TIMESTAMP     NOT NULL,
    updated_at          TIMESTAMP     NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_commissions_payment UNIQUE (payment_id)
);

CREATE TABLE commission_rules
(
    id              UUID PRIMARY KEY,
    category_id     UUID REFERENCES categories (id),
    percentage      NUMERIC(5, 2) NOT NULL,
    effective_from  TIMESTAMP     NOT NULL,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    version         BIGINT        NOT NULL DEFAULT 0
);

-- Règle de commission globale par défaut (12%)
INSERT INTO commission_rules (id, category_id, percentage, effective_from, active, created_at, updated_at, created_by, updated_by, deleted, version)
VALUES (gen_random_uuid(), NULL, 12.0, now(), true, now(), now(), 'system', 'system', false, 0);

CREATE TABLE payouts
(
    id              UUID PRIMARY KEY,
    professional_id UUID          NOT NULL REFERENCES professional_profiles (id),
    amount          NUMERIC(12, 2),
    currency        VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    reference       VARCHAR(255),
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    version         BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_payouts_professional ON payouts (professional_id);

CREATE TABLE invoices
(
    id              UUID PRIMARY KEY,
    payment_id      UUID         NOT NULL REFERENCES payments (id),
    invoice_number  VARCHAR(50)  NOT NULL,
    pdf_url         TEXT         NOT NULL,
    issued_at       TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_invoices_payment UNIQUE (payment_id),
    CONSTRAINT uq_invoices_number UNIQUE (invoice_number)
);
