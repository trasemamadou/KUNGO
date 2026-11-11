package com.kungo.booking.event;

import java.util.UUID;

/**
 * Publié quand les deux parties ont confirmé la fin de la prestation. Écouté par
 * le module {@code payment} pour déclencher automatiquement le versement au
 * professionnel (voir {@code PayoutAutomationListener}).
 */
public record BookingCompletedEvent(UUID bookingId) {
}
