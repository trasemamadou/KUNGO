package com.kungo.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Rappelle périodiquement aux deux parties de confirmer la fin d'une prestation
 * dont la date est dépassée. La réservation ne passe à {@code COMPLETED} que
 * lorsque le client ET le professionnel ont explicitement confirmé — jamais
 * automatiquement (règle métier : double confirmation obligatoire).
 */
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedRate = 3600000) // toutes les heures
    public void sendCompletionReminders() {
        bookingService.sendCompletionReminders();
    }
}
