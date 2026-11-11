-- ============================================================================
-- V11: Documents générés automatiquement au fil de la chaîne de réservation
-- (contrat à l'acceptation, reçu au versement du paiement au professionnel).
-- ============================================================================

CREATE TABLE booking_documents
(
    id              UUID PRIMARY KEY,
    booking_id      UUID         NOT NULL REFERENCES bookings (id),
    type            VARCHAR(20)  NOT NULL,
    document_number VARCHAR(50)  NOT NULL UNIQUE,
    pdf_url         TEXT         NOT NULL,
    generated_at    TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_documents_booking ON booking_documents (booking_id);
CREATE UNIQUE INDEX uq_booking_documents_booking_type ON booking_documents (booking_id, type) WHERE deleted = FALSE;
