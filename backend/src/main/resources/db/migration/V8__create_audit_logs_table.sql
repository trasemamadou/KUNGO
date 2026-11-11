-- ============================================================================
-- V8: Journal d'audit
-- ============================================================================

CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY,
    entity_name VARCHAR(100)  NOT NULL,
    entity_id   UUID,
    action      VARCHAR(100)  NOT NULL,
    performed_by UUID,
    details     VARCHAR(2000),
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    version     BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_name, entity_id);
CREATE INDEX idx_audit_logs_performed_by ON audit_logs (performed_by);
