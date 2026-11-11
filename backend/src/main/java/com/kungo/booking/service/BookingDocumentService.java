package com.kungo.booking.service;

import com.kungo.booking.entity.BookingDocument;
import com.kungo.booking.dto.BookingDocumentResponse;
import java.util.List;
import java.util.UUID;

public interface BookingDocumentService {

    /** Génère le contrat PDF de la réservation (idempotent : renvoie le document existant si déjà généré). */
    BookingDocument generateContract(UUID bookingId);

    /** Génère le reçu PDF de la réservation une fois le versement effectué (idempotent). */
    BookingDocument generateReceipt(UUID bookingId);

    List<BookingDocumentResponse> list(UUID bookingId);
}
