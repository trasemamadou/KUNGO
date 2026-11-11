-- ============================================================================
-- V12: Avis multi-critères — décompose la note globale en 5 dimensions
-- (ponctualité, qualité, communication, rapport qualité/prix, professionnalisme).
-- La colonne `rating` existante est conservée comme moyenne des 5 critères.
-- ============================================================================

ALTER TABLE reviews
    ADD COLUMN punctuality     INTEGER,
    ADD COLUMN quality         INTEGER,
    ADD COLUMN communication   INTEGER,
    ADD COLUMN value_for_money INTEGER,
    ADD COLUMN professionalism INTEGER;

UPDATE reviews
SET punctuality     = rating,
    quality         = rating,
    communication   = rating,
    value_for_money = rating,
    professionalism = rating
WHERE punctuality IS NULL;

ALTER TABLE reviews
    ALTER COLUMN punctuality SET NOT NULL,
    ALTER COLUMN quality SET NOT NULL,
    ALTER COLUMN communication SET NOT NULL,
    ALTER COLUMN value_for_money SET NOT NULL,
    ALTER COLUMN professionalism SET NOT NULL;
