-- ============================================================================
-- V2: Catégories & sous-catégories (auto-référencées)
-- ============================================================================

CREATE TABLE categories
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    slug          VARCHAR(160) NOT NULL,
    description   VARCHAR(500),
    icon          TEXT,
    image         TEXT,
    parent_id     UUID REFERENCES categories (id),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_categories_slug UNIQUE (slug)
);

CREATE INDEX idx_categories_slug ON categories (slug);
CREATE INDEX idx_categories_parent ON categories (parent_id);

-- Catégories racines de démarrage (métiers événementiels demandés dans le cahier des charges)
INSERT INTO categories (id, name, slug, description, active, display_order, created_at, updated_at, created_by, updated_by, deleted, version)
VALUES
    (gen_random_uuid(), 'Musique & Live', 'musique-live', 'Musiciens, chanteurs, groupes, DJ, animateurs', true, 1, now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'Animation & MC', 'animation-mc', 'Maîtres de cérémonie, animateurs, humoristes', true, 2, now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'Image & Son', 'image-son', 'Photographes, vidéastes', true, 3, now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'Décoration & Traiteur', 'decoration-traiteur', 'Décorateurs, traiteurs', true, 4, now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'Culture & Spiritualité', 'culture-spiritualite', 'Griots, personnalités religieuses, chorales', true, 5, now(), now(), 'system', 'system', false, 0),
    (gen_random_uuid(), 'Conférence & Coaching', 'conference-coaching', 'Conférenciers, coachs, influenceurs', true, 6, now(), now(), 'system', 'system', false, 0);
