package com.kungo.booking.event;

import java.util.UUID;

/** Publié quand une contre-proposition est soumise, pour notifier l'autre partie. */
public record BookingProposalSubmittedEvent(UUID bookingId, UUID proposalId, UUID submittedByUserId) {
}
