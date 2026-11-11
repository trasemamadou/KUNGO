package com.kungo.booking.event;

import java.util.UUID;

/** Publié pour rappeler à une réservation passée non confirmée que la confirmation de fin est attendue. */
public record BookingCompletionReminderEvent(UUID bookingId) {
}
