// Delta: Closing the Specification Gap — Chapter 4, Recipe 2
package com.yourcompany.payments;

/**
 * Records processed event IDs so that a webhook delivered twice (Stripe
 * retries on non-2xx responses) does not produce double side effects.
 *
 * Typical implementation: a database table with a unique constraint on
 * event_id. The unique constraint is the actual idempotency guarantee;
 * existsByEventId is an optimisation that lets the controller short-circuit
 * before the business logic runs.
 */
public interface IdempotencyRepository {

    /** True if save() has previously been called with this event id. */
    boolean existsByEventId(String eventId);

    /**
     * Records that an event has been processed.
     * Implementations should use the underlying unique constraint to
     * detect duplicates that race past the existsByEventId check.
     */
    void save(String eventId);
}
