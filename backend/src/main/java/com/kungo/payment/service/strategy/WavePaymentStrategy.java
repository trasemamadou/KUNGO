package com.kungo.payment.service.strategy;

import com.kungo.common.domain.Money;
import com.kungo.payment.domain.enums.PaymentProvider;
import com.kungo.payment.domain.enums.PaymentStatus;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Intégration Wave (Sénégal/Afrique de l'Ouest). Wave ne propose pas de SDK public
 * standard : en production, remplacer ce squelette par un appel HTTP à l'API
 * "Wave Checkout" du marchand (création de session de paiement + vérification par
 * webhook signé). Ce placeholder crée une référence de transaction en attente,
 * confirmée ensuite via {@code PaymentWebhookController#confirmManualProvider}.
 */
@Slf4j
@Component
public class WavePaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.WAVE;
    }

    @Override
    public PaymentChargeResult charge(String bookingReference, Money amount) {
        String reference = "WAVE-" + UUID.randomUUID();
        log.warn("Wave integration is a placeholder (TODO: real merchant API) — reference {}", reference);
        return new PaymentChargeResult(reference, PaymentStatus.PENDING, null);
    }

    @Override
    public void refund(String transactionRef) {
        log.warn("Wave refund is a placeholder (TODO: real merchant API) for {}", transactionRef);
    }
}
