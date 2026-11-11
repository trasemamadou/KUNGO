-- ============================================================================
-- V3: Profils professionnels, compétences, langues, médias, disponibilités
-- ============================================================================

CREATE TABLE professional_profiles
(
    id                   UUID PRIMARY KEY,
    user_id              UUID          NOT NULL REFERENCES users (id),
    business_name        VARCHAR(150),
    biography            VARCHAR(3000),
    years_experience     INTEGER       NOT NULL DEFAULT 0,
    country              VARCHAR(100),
    region               VARCHAR(100),
    city                 VARCHAR(100),
    street               VARCHAR(255),
    latitude             NUMERIC(10, 7),
    longitude            NUMERIC(10, 7),
    verification_status  VARCHAR(20)   NOT NULL DEFAULT 'UNVERIFIED',
    premium              BOOLEAN       NOT NULL DEFAULT FALSE,
    premium_expires_at   TIMESTAMP,
    average_rating       NUMERIC(3, 2) NOT NULL DEFAULT 0,
    total_reviews        INTEGER       NOT NULL DEFAULT 0,
    total_bookings       INTEGER       NOT NULL DEFAULT 0,
    response_time_hours  INTEGER,
    created_at           TIMESTAMP     NOT NULL,
    updated_at           TIMESTAMP     NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    deleted              BOOLEAN       NOT NULL DEFAULT FALSE,
    version              BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_professional_profiles_user UNIQUE (user_id)
);

CREATE INDEX idx_professional_profiles_user ON professional_profiles (user_id);
CREATE INDEX idx_professional_profiles_verification ON professional_profiles (verification_status);
CREATE INDEX idx_professional_profiles_city ON professional_profiles (city);

CREATE TABLE skills
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_skills_name UNIQUE (name)
);

CREATE TABLE languages
(
    id         UUID PRIMARY KEY,
    code       VARCHAR(5)   NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_languages_code UNIQUE (code)
);

CREATE TABLE professional_skills
(
    professional_id UUID NOT NULL REFERENCES professional_profiles (id),
    skill_id        UUID NOT NULL REFERENCES skills (id),
    PRIMARY KEY (professional_id, skill_id)
);

CREATE TABLE professional_languages
(
    professional_id UUID NOT NULL REFERENCES professional_profiles (id),
    language_id      UUID NOT NULL REFERENCES languages (id),
    PRIMARY KEY (professional_id, language_id)
);

CREATE TABLE social_links
(
    id              UUID PRIMARY KEY,
    professional_id UUID        NOT NULL REFERENCES professional_profiles (id),
    platform        VARCHAR(20) NOT NULL,
    url             TEXT        NOT NULL,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE professional_media
(
    id              UUID PRIMARY KEY,
    professional_id UUID        NOT NULL REFERENCES professional_profiles (id),
    type            VARCHAR(10) NOT NULL,
    url             TEXT        NOT NULL,
    display_order   INTEGER     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE availabilities
(
    id              UUID PRIMARY KEY,
    professional_id UUID      NOT NULL REFERENCES professional_profiles (id),
    specific_date   DATE,
    day_of_week     VARCHAR(10),
    start_time      TIME      NOT NULL,
    end_time        TIME      NOT NULL,
    recurring       BOOLEAN   NOT NULL DEFAULT FALSE,
    blocked         BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN   NOT NULL DEFAULT FALSE,
    version         BIGINT    NOT NULL DEFAULT 0
);

CREATE INDEX idx_availabilities_professional ON availabilities (professional_id);
CREATE INDEX idx_availabilities_date ON availabilities (specific_date);

-- Langues de démarrage
INSERT INTO languages (id, code, name, created_at, updated_at, created_by, updated_by, deleted, version)
VALUES
    (gen_random_uuid(), 'fr', 'Français', now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'en', 'Anglais', now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'wo', 'Wolof', now(), now(), 'system', 'system', false, 0);
