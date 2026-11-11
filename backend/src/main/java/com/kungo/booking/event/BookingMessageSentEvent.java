package com.kungo.booking.event;

import java.util.UUID;

/**
 * Publié à chaque nouveau message envoyé dans le fil de discussion d'une réservation.
 * Le module {@code notification} écoute cet événement pour notifier le destinataire.
 */
public record BookingMessageSentEvent(UUID bookingId, UUID messageId, UUID senderId) {
}
