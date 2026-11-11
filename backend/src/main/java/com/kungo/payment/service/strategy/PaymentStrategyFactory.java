package com.kungo.payment.service.strategy;

import com.kungo.common.exception.BadRequestException;
import com.kungo.payment.domain.enums.PaymentProvider;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Résout la {@link PaymentStrategy} appropriée pour un {@link PaymentProvider}
 * (pattern Factory). {@code CARD} est routé vers Stripe, qui gère nativement les
 * paiements par carte bancaire.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentProvider, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> allStrategies, StripePaymentStrategy stripeStrategy) {
        this.strategies = allStrategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::getProvider, Function.identity()));
        this.strategies.put(PaymentProvider.CARD, stripeStrategy);
    }

    public PaymentStrategy resolve(PaymentProvider provider) {
        PaymentStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new BadRequestException("payment.failed");
        }
        return strategy;
    }
}
