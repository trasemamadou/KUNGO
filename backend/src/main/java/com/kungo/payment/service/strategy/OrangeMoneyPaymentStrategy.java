package com.kungo.payment.service.strategy;

import com.kungo.common.domain.Money;
import com.kungo.payment.domain.enums.PaymentProvider;
import com.kungo.payment.domain.enums.PaymentStatus;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Intégration Orange Money. Placeholder — en production, appeler l'API Orange
 * Money Web Payment (OAuth2 + création de transaction + webhook de confirmation).
 * Voir {@link WavePaymentStrategy} pour la même remarque.
 */
@Slf4j
@Component
public class OrangeMoneyPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.ORANGE_MONEY;
    }

    @Override
    public PaymentChargeResult charge(String bookingReference, Money amount) {
        String reference = "OM-" + UUID.randomUUID();
        log.warn("Orange Money integration is a placeholder (TODO: real merchant API) — reference {}", reference);
        return new PaymentChargeResult(reference, PaymentStatus.PENDING, null);
    }

    @Override
    public void refund(String transactionRef) {
        log.warn("Orange Money refund is a placeholder (TODO: real merchant API) for {}", transactionRef);
    }
}
