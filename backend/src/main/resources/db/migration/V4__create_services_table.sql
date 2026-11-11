-- ============================================================================
-- V4: Prestations (services) & médias associés
-- ============================================================================

CREATE TABLE services
(
    id               UUID PRIMARY KEY,
    professional_id  UUID          NOT NULL REFERENCES professional_profiles (id),
    category_id      UUID          NOT NULL REFERENCES categories (id),
    title            VARCHAR(150)  NOT NULL,
    description      VARCHAR(3000),
    price_type       VARCHAR(20)   NOT NULL,
    amount           NUMERIC(12, 2),
    currency         VARCHAR(3)    NOT NULL DEFAULT 'XOF',
    duration_minutes INTEGER,
    conditions       VARCHAR(2000),
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
    version          BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_services_professional ON services (professional_id);
CREATE INDEX idx_services_category ON services (category_id);
CREATE INDEX idx_services_status ON services (status);

CREATE TABLE service_media
(
    id            UUID PRIMARY KEY,
    service_id    UUID        NOT NULL REFERENCES services (id),
    type          VARCHAR(10) NOT NULL,
    url           TEXT        NOT NULL,
    display_order INTEGER     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP   NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    version       BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_service_media_service ON service_media (service_id);
