-- ============================================================================
-- V10: Rattache les reversements (payouts) à la réservation à l'origine du
-- versement, pour permettre le règlement automatique à la double confirmation
-- de fin de prestation (et garantir l'idempotence : un seul payout par booking).
-- ============================================================================

ALTER TABLE payouts
    ADD COLUMN booking_id UUID REFERENCES bookings (id);

CREATE UNIQUE INDEX uq_payouts_booking ON payouts (booking_id) WHERE booking_id IS NOT NULL;
