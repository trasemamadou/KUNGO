package com.kungo.payment.event;

import java.util.UUID;

public record PaymentSucceededEvent(UUID bookingId, UUID paymentId) {
}
