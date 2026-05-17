package com.fastpos.controller;

import com.fastpos.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (paymentIntent != null) {
                    paymentService.handleStripeWebhook(paymentIntent.getId(), "succeeded");
                    log.info("Stripe webhook: payment_intent.succeeded for {}", paymentIntent.getId());
                }
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (paymentIntent != null) {
                    paymentService.handleStripeWebhook(paymentIntent.getId(), "payment_failed");
                    log.warn("Stripe webhook: payment_failed for {}", paymentIntent.getId());
                }
            }

            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Stripe webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }
}
