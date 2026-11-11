package com.kungo.payment.service.strategy;

import com.kungo.payment.domain.enums.PaymentStatus;

/**
 * Résultat de l'initiation d'un paiement auprès d'un fournisseur.
 * {@code clientSecret} n'est renseigné que pour les fournisseurs nécessitant une
 * confirmation côté client (Stripe PaymentIntent) ; {@code null} sinon.
 */
public record PaymentChargeResult(String transactionRef, PaymentStatus status, String clientSecret) {
}
