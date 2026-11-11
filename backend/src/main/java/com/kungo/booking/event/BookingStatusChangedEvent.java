package com.kungo.booking.event;

import com.kungo.booking.enums.BookingStatus;
import java.util.UUID;

/**
 * Publié à chaque transition de statut d'une réservation. Le module
 * {@code notification} écoute cet événement pour notifier les parties concernées
 * (email + WebSocket) sans que {@code booking} n'ait à connaître ces mécaniques.
 */
public record BookingStatusChangedEvent(UUID bookingId, BookingStatus fromStatus, BookingStatus toStatus) {
}
