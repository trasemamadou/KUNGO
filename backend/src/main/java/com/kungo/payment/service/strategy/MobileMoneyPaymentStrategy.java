package com.kungo.payment.service.strategy;

import com.kungo.common.domain.Money;
import com.kungo.payment.domain.enums.PaymentProvider;
import com.kungo.payment.domain.enums.PaymentStatus;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mobile Money générique (MTN MoMo, Moov, etc.). Placeholder — voir
 * {@link WavePaymentStrategy}.
 */
@Slf4j
@Component
public class MobileMoneyPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.MOBILE_MONEY;
    }

    @Override
    public PaymentChargeResult charge(String bookingReference, Money amount) {
        String reference = "MOMO-" + UUID.randomUUID();
        log.warn("Mobile Money integration is a placeholder (TODO: real merchant API) — reference {}", reference);
        return new PaymentChargeResult(reference, PaymentStatus.PENDING, null);
    }

    @Override
    public void refund(String transactionRef) {
        log.warn("Mobile Money refund is a placeholder (TODO: real merchant API) for {}", transactionRef);
    }
}
