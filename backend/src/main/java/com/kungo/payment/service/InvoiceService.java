package com.kungo.payment.service;

import com.kungo.booking.entity.Booking;
import com.kungo.payment.domain.entity.Payment;

public interface InvoiceService {

    /**
     * Génère la facture PDF d'un paiement réussi et retourne l'entité persistée.
     */
    com.kungo.payment.domain.entity.Invoice generate(Payment payment, Booking booking);
}
