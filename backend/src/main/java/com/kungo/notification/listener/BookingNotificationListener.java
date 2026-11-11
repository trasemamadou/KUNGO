package com.kungo.notification.listener;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.event.BookingCompletionReminderEvent;
import com.kungo.booking.event.BookingMessageSentEvent;
import com.kungo.booking.event.BookingProposalSubmittedEvent;
import com.kungo.booking.event.BookingStatusChangedEvent;
import com.kungo.booking.service.BookingService;
import com.kungo.notification.domain.enums.NotificationType;
import com.kungo.notification.service.NotificationService;
import com.kungo.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Traduit les événements du module {@code booking} (découplé via des events Spring)
 * en notifications pour le client et/ou le professionnel concernés.
 */
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final BookingService bookingService;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingStatusChanged(BookingStatusChangedEvent event) {
        Booking booking = bookingService.getEntityById(event.bookingId());
        User client = booking.getClient();
        User professional = booking.getProfessional().getUser();
        String link = "/bookings/" + booking.getId();
        String title = booking.getService().getTitle();

        switch (event.toStatus()) {
            case PENDING -> notificationService.notify(professional, NotificationType.BOOKING_CREATED,
                    "Nouvelle demande de réservation",
                    "Vous avez reçu une nouvelle demande pour \"" + title + "\".", link);
            case ACCEPTED -> notificationService.notify(client, NotificationType.BOOKING_ACCEPTED,
                    "Réservation acceptée",
                    "Votre demande a été acceptée. Vous pouvez procéder au paiement.", link);
            case REJECTED -> notificationService.notify(client, NotificationType.BOOKING_REJECTED,
                    "Réservation refusée",
                    "Votre demande de réservation a été refusée.", link);
            case PREPARING -> notificationService.notify(client, NotificationType.BOOKING_CONFIRMED,
                    "Prestation en préparation",
                    "Le professionnel prépare votre prestation \"" + title + "\".", link);
            case EN_ROUTE -> notificationService.notify(client, NotificationType.BOOKING_CONFIRMED,
                    "Le professionnel est en route",
                    "Le professionnel se dirige vers le lieu de l'événement.", link);
            case ARRIVED -> notificationService.notify(client, NotificationType.BOOKING_CONFIRMED,
                    "Le professionnel est arrivé",
                    "Le professionnel est arrivé sur le lieu de l'événement.", link);
            case IN_PROGRESS -> {
                notificationService.notify(client, NotificationType.BOOKING_CONFIRMED,
                        "Prestation en cours", "Votre prestation \"" + title + "\" a débuté.", link);
                notificationService.notify(professional, NotificationType.BOOKING_CONFIRMED,
                        "Prestation en cours", "Votre prestation \"" + title + "\" a débuté.", link);
            }
            case CANCELLED -> {
                notificationService.notify(client, NotificationType.BOOKING_CANCELLED,
                        "Réservation annulée", "La réservation a été annulée.", link);
                notificationService.notify(professional, NotificationType.BOOKING_CANCELLED,
                        "Réservation annulée", "La réservation a été annulée.", link);
            }
            case COMPLETED -> {
                notificationService.notify(client, NotificationType.BOOKING_COMPLETED,
                        "Prestation terminée", "N'oubliez pas de noter votre expérience !", link);
                notificationService.notify(professional, NotificationType.BOOKING_COMPLETED,
                        "Prestation terminée", "N'oubliez pas de noter votre client !", link);
            }
            case PAYOUT_SETTLED -> notificationService.notify(professional, NotificationType.PAYMENT_SUCCEEDED,
                    "Paiement versé", "Le montant net de la prestation \"" + title + "\" vous a été versé.", link);
            default -> { /* PAID géré par PaymentNotificationListener, NEGOTIATING par onProposalSubmitted */ }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalSubmitted(BookingProposalSubmittedEvent event) {
        Booking booking = bookingService.getEntityById(event.bookingId());
        String link = "/bookings/" + booking.getId();
        boolean submittedByClient = booking.getClient().getId().equals(event.submittedByUserId());
        User recipient = submittedByClient ? booking.getProfessional().getUser() : booking.getClient();

        notificationService.notify(recipient, NotificationType.BOOKING_DATE_PROPOSED,
                "Nouvelle contre-proposition",
                "Une nouvelle proposition a été soumise pour \"" + booking.getService().getTitle() + "\".", link);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompletionReminder(BookingCompletionReminderEvent event) {
        Booking booking = bookingService.getEntityById(event.bookingId());
        String link = "/bookings/" + booking.getId();

        if (booking.getClientConfirmedAt() == null) {
            notificationService.notify(booking.getClient(), NotificationType.BOOKING_COMPLETED,
                    "Confirmez la fin de votre prestation",
                    "Merci de confirmer que la prestation \"" + booking.getService().getTitle() + "\" s'est bien déroulée.", link);
        }
        if (booking.getProfessionalConfirmedAt() == null) {
            notificationService.notify(booking.getProfessional().getUser(), NotificationType.BOOKING_COMPLETED,
                    "Confirmez la fin de la prestation",
                    "Merci de confirmer que la prestation \"" + booking.getService().getTitle() + "\" est terminée.", link);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSent(BookingMessageSentEvent event) {
        Booking booking = bookingService.getEntityById(event.bookingId());
        String link = "/bookings/" + booking.getId();
        boolean sentByClient = booking.getClient().getId().equals(event.senderId());
        User recipient = sentByClient ? booking.getProfessional().getUser() : booking.getClient();

        notificationService.notify(recipient, NotificationType.NEW_MESSAGE,
                "Nouveau message",
                "Vous avez reçu un nouveau message concernant \"" + booking.getService().getTitle() + "\".", link);
    }
}
