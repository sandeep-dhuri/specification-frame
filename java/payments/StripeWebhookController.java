// Delta: Closing the Specification Gap — Chapter 4, Recipe 2
// Idempotent Stripe webhook handler in Java / Spring Boot 3.3
// The order-of-operations constraint is the critical constraint from Chapter 4:
// (1) verify → (2) check idempotency → (3) business logic → (4) mark processed
package com.yourcompany.payments;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.yourcompany.common.Money;
import com.yourcompany.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhooks/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final PaymentService       paymentService;
    private final IdempotencyRepository idempotencyRepo;
    private final String               webhookSecret;

    public StripeWebhookController(
            PaymentService paymentService,
            IdempotencyRepository idempotencyRepo,
            @Value("${stripe.webhook.secret}") String webhookSecret) {
        this.paymentService  = paymentService;
        this.idempotencyRepo = idempotencyRepo;
        this.webhookSecret   = webhookSecret;
    }

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        // STEP 1: Verify signature FIRST — before any processing
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("stripe_signature_invalid");
            return ResponseEntity.badRequest().body(Map.of("received", false));
        }

        // STEP 2: Check idempotency BEFORE business logic
        if (idempotencyRepo.existsByEventId(event.getId())) {
            log.info("webhook_duplicate_skipped event_id={}", event.getId());
            return ResponseEntity.ok(Map.of("received", true));
        }

        // STEP 3: Execute business logic
        if ("payment_intent.succeeded".equals(event.getType())) {
            var paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();

            // Money.of(String, Currency) — NEVER new Money(double, ...)
            var amount = Money.of(
                BigDecimal.valueOf(paymentIntent.getAmount()).movePointLeft(2).toPlainString(),
                Currency.getInstance(paymentIntent.getCurrency().toUpperCase())
            );

            Result<Void> result = paymentService.processSucceeded(
                paymentIntent.getId(), amount);

            if (result.isFailure()) {
                log.error("payment_processing_failed event_id={} code={}",
                    event.getId(), result.getErrorCode());
                return ResponseEntity.internalServerError().body(Map.of("received", false));
            }
        }

        // STEP 4: Mark processed AFTER business logic succeeds
        idempotencyRepo.save(event.getId());

        // event.getId() is safe to log — no PCI card data in log messages
        log.info("webhook_processed event_id={} type={}", event.getId(), event.getType());
        return ResponseEntity.ok(Map.of("received", true));
    }
}
