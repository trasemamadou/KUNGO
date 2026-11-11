package com.kungo.booking.enums;

import com.kungo.booking.entity.Booking;

/**
 * Cycle de vie complet d'une réservation, du dépôt de la demande jusqu'au
 * versement du prestataire. Voir {@link Booking}
 * pour les transitions autorisées entre ces états.
 */
public enum BookingStatus {
    PENDING,
    NEGOTIATING,
    ACCEPTED,
    REJECTED,
    PAID,
    PREPARING,
    EN_ROUTE,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    PAYOUT_SETTLED,
    CANCELLED
}
