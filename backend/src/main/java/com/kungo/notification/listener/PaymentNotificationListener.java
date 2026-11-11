package com.kungo.notification.listener;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.service.BookingService;
import com.kungo.notification.domain.enums.NotificationType;
import com.kungo.notification.service.NotificationService;
import com.kungo.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentNotificationListener {

    private final BookingService bookingService;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        Booking booking = bookingService.getEntityById(event.bookingId());
        String link = "/bookings/" + booking.getId();

        notificationService.notify(booking.getClient(), NotificationType.PAYMENT_SUCCEEDED,
                "Paiement confirmé",
                "Votre paiement pour \"" + booking.getService().getTitle() + "\" a été confirmé.", link);

        notificationService.notify(booking.getProfessional().getUser(), NotificationType.PAYMENT_SUCCEEDED,
                "Réservation payée",
                "Le client a payé la réservation \"" + booking.getService().getTitle() + "\".", link);
    }
}
