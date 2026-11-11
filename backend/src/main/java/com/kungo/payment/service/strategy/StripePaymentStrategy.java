package com.kungo.payment.service.strategy;

import com.kungo.common.domain.Money;
import com.kungo.common.exception.BadRequestException;
import com.kungo.payment.domain.enums.PaymentProvider;
import com.kungo.payment.domain.enums.PaymentStatus;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Intégration réelle Stripe via le SDK officiel (PaymentIntent). Le paiement n'est
 * confirmé côté serveur que par webhook ({@code payment_intent.succeeded}) — voir
 * {@code PaymentWebhookController} — jamais par simple retour d'API synchrone, pour
 * éviter qu'un client malveillant ne déclenche une confirmation sans paiement réel.
 */
@Slf4j
@Component
public class StripePaymentStrategy implements PaymentStrategy {

    @Value("${app.payment.stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    void init() {
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentChargeResult charge(String bookingReference, Money amount) {
        try {
            long amountInSmallestUnit = amount.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(amount.getCurrency().toLowerCase())
                    .putMetadata("bookingReference", bookingReference)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentChargeResult(intent.getId(), PaymentStatus.PENDING, intent.getClientSecret());
        } catch (StripeException e) {
            log.error("Stripe PaymentIntent creation failed", e);
            throw new BadRequestException("payment.failed");
        }
    }

    @Override
    public void refund(String transactionRef) {
        try {
            RefundCreateParams params = RefundCreateParams.builder().setPaymentIntent(transactionRef).build();
            Refund.create(params);
        } catch (StripeException e) {
            log.error("Stripe refund failed for {}", transactionRef, e);
            throw new BadRequestException("payment.failed");
        }
    }
}
