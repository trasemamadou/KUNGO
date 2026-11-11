-- ============================================================================
-- V9: Chaîne de réservation complète — demande enrichie, négociation par
-- contre-propositions, suivi de prestation granulaire, double confirmation,
-- pièces jointes.
-- ============================================================================

ALTER TABLE bookings RENAME COLUMN client_message TO description;
ALTER TABLE bookings DROP COLUMN proposed_date;

ALTER TABLE bookings
    ADD COLUMN event_type                 VARCHAR(100),
    ADD COLUMN estimated_duration_minutes INTEGER,
    ADD COLUMN participant_count          INTEGER,
    ADD COLUMN special_requests           VARCHAR(1000),
    ADD COLUMN client_confirmed_at        TIMESTAMP,
    ADD COLUMN professional_confirmed_at  TIMESTAMP;

CREATE TABLE booking_proposals
(
    id                        UUID PRIMARY KEY,
    booking_id                UUID          NOT NULL REFERENCES bookings (id),
    proposed_by               UUID          NOT NULL REFERENCES users (id),
    proposed_price_amount     NUMERIC(12, 2),
    proposed_price_currency   VARCHAR(3),
    proposed_event_date       TIMESTAMP,
    proposed_duration_minutes INTEGER,
    proposed_conditions       VARCHAR(2000),
    message                   VARCHAR(1000),
    status                    VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at                TIMESTAMP     NOT NULL,
    updated_at                TIMESTAMP     NOT NULL,
    created_by                VARCHAR(100),
    updated_by                VARCHAR(100),
    deleted                   BOOLEAN       NOT NULL DEFAULT FALSE,
    version                   BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_proposals_booking ON booking_proposals (booking_id);

CREATE TABLE booking_attachments
(
    id          UUID PRIMARY KEY,
    booking_id  UUID         NOT NULL REFERENCES bookings (id),
    uploaded_by UUID         NOT NULL REFERENCES users (id),
    file_name   VARCHAR(255) NOT NULL,
    url         TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_attachments_booking ON booking_attachments (booking_id);
