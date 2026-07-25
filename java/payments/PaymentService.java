// Delta: Closing the Specification Gap — Chapter 4, Recipe 2
package com.yourcompany.payments;

import com.yourcompany.common.Money;
import com.yourcompany.common.Result;

/**
 * Application service for processing payment events.
 * Concrete implementation belongs in a separate module — the controller
 * interacts with payments only through this interface so that the
 * verify → idempotency → business logic → mark-processed sequence
 * stays testable in isolation.
 */
public interface PaymentService {

    /**
     * Processes a successful payment. Should be idempotent in its own right;
     * the controller layer also enforces idempotency via IdempotencyRepository.
     *
     * @param paymentIntentId Stripe payment_intent.id (e.g. "pi_3Abc...")
     * @param amount          monetary amount in the captured currency
     * @return success Result on completion, or a Result.failure carrying a
     *                 stable error code if a downstream call fails
     */
    Result<Void> processSucceeded(String paymentIntentId, Money amount);
}
