-- ============================================================================
-- V5: Réservations & historique de statuts
-- ============================================================================

CREATE TABLE bookings
(
    id                   UUID PRIMARY KEY,
    client_id            UUID          NOT NULL REFERENCES users (id),
    professional_id      UUID          NOT NULL REFERENCES professional_profiles (id),
    service_id           UUID          NOT NULL REFERENCES services (id),
    event_date           TIMESTAMP     NOT NULL,
    event_address        VARCHAR(500),
    status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    proposed_date        TIMESTAMP,
    client_message       VARCHAR(1000),
    professional_message VARCHAR(1000),
    amount               NUMERIC(12, 2),
    currency             VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    commission_amount    NUMERIC(12, 2),
    professional_amount  NUMERIC(12, 2),
    created_at           TIMESTAMP     NOT NULL,
    updated_at           TIMESTAMP     NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    deleted              BOOLEAN       NOT NULL DEFAULT FALSE,
    version              BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_bookings_client ON bookings (client_id);
CREATE INDEX idx_bookings_professional ON bookings (professional_id);
CREATE INDEX idx_bookings_status ON bookings (status);
CREATE INDEX idx_bookings_event_date ON bookings (event_date);

CREATE TABLE booking_status_history
(
    id          UUID PRIMARY KEY,
    booking_id  UUID        NOT NULL REFERENCES bookings (id),
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    changed_by  UUID,
    comment     VARCHAR(500),
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    version     BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_status_history_booking ON booking_status_history (booking_id);
