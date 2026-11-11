package com.kungo.payment.service.strategy;

import com.kungo.common.domain.Money;
import com.kungo.payment.domain.enums.PaymentProvider;

/**
 * Stratégie d'intégration à un fournisseur de paiement (pattern Strategy).
 * Une implémentation par {@link PaymentProvider} — voir {@code PaymentStrategyFactory}
 * pour la résolution à l'exécution.
 */
public interface PaymentStrategy {

    PaymentProvider getProvider();

    PaymentChargeResult charge(String bookingReference, Money amount);

    void refund(String transactionRef);
}
