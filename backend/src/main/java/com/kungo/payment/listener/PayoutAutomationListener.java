package com.kungo.payment.listener;

import com.kungo.booking.event.BookingCompletedEvent;
import com.kungo.payment.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Déclenche automatiquement le versement au professionnel dès que la double
 * confirmation de fin de prestation est obtenue (voir {@code Booking#clientConfirmCompletion}
 * / {@code professionalConfirmCompletion}), sans intervention manuelle de l'admin.
 */
@Component
@RequiredArgsConstructor
public class PayoutAutomationListener {

    private final PayoutService payoutService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCompleted(BookingCompletedEvent event) {
        payoutService.settleForCompletedBooking(event.bookingId());
    }
}
