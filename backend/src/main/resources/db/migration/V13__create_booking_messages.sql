-- ============================================================================
-- V13: Messagerie liée à la demande de réservation — fil de discussion entre
-- le client et le professionnel, avec pièces jointes et statut de lecture.
-- ============================================================================

CREATE TABLE booking_messages
(
    id              UUID PRIMARY KEY,
    booking_id      UUID         NOT NULL REFERENCES bookings (id),
    sender_id       UUID         NOT NULL REFERENCES users (id),
    content         VARCHAR(2000),
    attachment_url  TEXT,
    attachment_name VARCHAR(255),
    read            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_messages_booking ON booking_messages (booking_id);
