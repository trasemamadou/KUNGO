package com.kungo.payment.service;

import com.kungo.payment.dto.response.PayoutResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PayoutService {

    List<PayoutResponse> listForProfessional(UUID professionalId);

    PayoutResponse create(UUID professionalId, BigDecimal amount, String currency);

    PayoutResponse markPaid(UUID payoutId, String reference);

    PayoutResponse markFailed(UUID payoutId);

    /**
     * Calcule et verse automatiquement le montant net d'une réservation terminée
     * (double confirmation obtenue) au professionnel. Idempotent : ne crée pas de
     * second versement si un {@code Payout} existe déjà pour cette réservation.
     */
    void settleForCompletedBooking(UUID bookingId);
}
