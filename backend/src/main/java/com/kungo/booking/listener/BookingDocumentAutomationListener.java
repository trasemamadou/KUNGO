package com.kungo.booking.listener;

import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.event.BookingStatusChangedEvent;
import com.kungo.booking.service.BookingDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Génère automatiquement le contrat (à l'acceptation) puis le reçu (une fois le
 * versement effectué au professionnel), sans action manuelle des parties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingDocumentAutomationListener {

    private final BookingDocumentService bookingDocumentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(BookingStatusChangedEvent event) {
        if (event.toStatus() == BookingStatus.ACCEPTED) {
            safeGenerate(() -> bookingDocumentService.generateContract(event.bookingId()));
        } else if (event.toStatus() == BookingStatus.PAYOUT_SETTLED) {
            safeGenerate(() -> bookingDocumentService.generateReceipt(event.bookingId()));
        }
    }

    private void safeGenerate(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Failed to auto-generate booking document", e);
        }
    }
}
