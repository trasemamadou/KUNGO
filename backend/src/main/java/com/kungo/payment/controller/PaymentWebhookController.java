package com.kungo.payment.controller;

import com.kungo.common.response.ApiResponse;
import com.kungo.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Points d'entrée de confirmation de paiement. {@code /stripe} est un vrai webhook
 * signé Stripe (accessible publiquement mais vérifié cryptographiquement). Les
 * endpoints {@code /manual/**} sont un palliatif réservé aux admins tant que les
 * intégrations Wave/Orange Money/Mobile Money n'ont pas de webhook réel branché
 * (voir TODO dans les classes {@code *PaymentStrategy} correspondantes).
 */
@Slf4j
@RestController
@RequestMapping("/payments/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhooks Paiement")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @Value("${app.payment.stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    @Operation(summary = "Webhook Stripe (payment_intent.succeeded / payment_intent.payment_failed)")
    public ResponseEntity<Void> stripeWebhook(@RequestBody String payload,
                                               @RequestHeader("Stripe-Signature") String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().build();
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject instanceof PaymentIntent intent) {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> paymentService.handleProviderSucceeded(intent.getId());
                case "payment_intent.payment_failed" -> paymentService.handleProviderFailed(intent.getId());
                default -> log.debug("Ignored Stripe event type {}", event.getType());
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/manual/{transactionRef}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Confirmer manuellement un paiement Wave/Orange Money/Mobile Money (admin, en attendant l'intégration réelle)")
    public ApiResponse<Void> confirmManual(@PathVariable @NotBlank String transactionRef) {
        paymentService.handleProviderSucceeded(transactionRef);
        return ApiResponse.message("Paiement confirmé manuellement");
    }
}
