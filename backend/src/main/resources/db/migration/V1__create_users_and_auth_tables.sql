-- ============================================================================
-- V1: Utilisateurs & Authentification
-- Toutes les tables métier partagent les colonnes d'audit de BaseEntity :
-- id (UUID, généré côté application par Hibernate), created_at, updated_at,
-- created_by, updated_by, deleted (soft delete), version (verrou optimiste).
-- ============================================================================

CREATE TABLE users
(
    id                 UUID PRIMARY KEY,
    email              VARCHAR(180) NOT NULL,
    password_hash      TEXT         NOT NULL,
    phone              VARCHAR(30),
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    role               VARCHAR(30)  NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    two_factor_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    avatar_url         TEXT,
    locale             VARCHAR(5)            DEFAULT 'fr',
    last_login_at      TIMESTAMP,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    version            BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_status ON users (status);

CREATE TABLE user_documents
(
    id               UUID PRIMARY KEY,
    user_id          UUID         NOT NULL REFERENCES users (id),
    type             VARCHAR(30)  NOT NULL,
    url              TEXT         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_documents_user ON user_documents (user_id);

CREATE TABLE refresh_tokens
(
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id),
    token       VARCHAR(512) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE activation_tokens
(
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id),
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_activation_tokens_token UNIQUE (token)
);

CREATE TABLE password_reset_tokens
(
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id),
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_password_reset_tokens_token UNIQUE (token)
);
