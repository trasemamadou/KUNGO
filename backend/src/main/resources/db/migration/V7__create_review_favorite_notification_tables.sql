-- ============================================================================
-- V7: Avis, signalements, favoris, notifications
-- ============================================================================

CREATE TABLE reviews
(
    id                     UUID PRIMARY KEY,
    booking_id             UUID        NOT NULL REFERENCES bookings (id),
    author_id              UUID        NOT NULL REFERENCES users (id),
    target_id              UUID        NOT NULL REFERENCES users (id),
    rating                 INTEGER     NOT NULL,
    comment                VARCHAR(2000),
    professional_response  VARCHAR(2000),
    moderation_status      VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at             TIMESTAMP   NOT NULL,
    updated_at             TIMESTAMP   NOT NULL,
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100),
    deleted                BOOLEAN     NOT NULL DEFAULT FALSE,
    version                BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_reviews_booking ON reviews (booking_id);
CREATE INDEX idx_reviews_target ON reviews (target_id);

CREATE TABLE reports
(
    id                  UUID PRIMARY KEY,
    reporter_id         UUID        NOT NULL REFERENCES users (id),
    reported_user_id    UUID REFERENCES users (id),
    reported_review_id  UUID REFERENCES reviews (id),
    reason              VARCHAR(1000) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP   NOT NULL,
    updated_at          TIMESTAMP   NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
    version             BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE favorites
(
    id              UUID PRIMARY KEY,
    client_id       UUID      NOT NULL REFERENCES users (id),
    professional_id UUID      NOT NULL REFERENCES professional_profiles (id),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN   NOT NULL DEFAULT FALSE,
    version         BIGINT    NOT NULL DEFAULT 0,
    CONSTRAINT uq_favorites_client_professional UNIQUE (client_id, professional_id)
);

CREATE INDEX idx_favorites_client ON favorites (client_id);
CREATE INDEX idx_favorites_professional ON favorites (professional_id);

CREATE TABLE notifications
(
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users (id),
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    message    VARCHAR(1000) NOT NULL,
    link       TEXT,
    read       BOOLEAN      NOT NULL DEFAULT FALSE,
    channel    VARCHAR(20)  NOT NULL,
    sent_at    TIMESTAMP,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_read ON notifications (read);
