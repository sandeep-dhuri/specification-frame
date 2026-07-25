// Delta: Closing the Specification Gap - Chapter 3 / Appendix B
package com.yourcompany.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable monetary value. Always use BigDecimal - never double or float for money.
 *
 * <pre>
 * // Correct
 * var price = Money.of("19.99", Currency.getInstance("USD"));
 * var total = price.add(tax);
 *
 * // NEVER - double arithmetic loses precision (19.99 is 19.990000000000001 in IEEE 754)
 * double price = 19.99;
 * </pre>
 *
 * CANONICAL MONEY POLICY (identical across all four language implementations):
 *   - Calculation scale: 4 decimal places
 *   - Display / storage scale: 2 decimal places
 *   - Rounding: HALF_UP
 *   - Currency mismatch: throws at runtime (C# prevents it at compile time via a type parameter).
 *
 * Note: getAmount() returns the value at 4dp calculation scale (e.g. 19.9900). For display,
 * storage, or API output, always use toDisplayAmount() which rounds to 2dp.
 */
public final class Money {

    private static final int          CALCULATION_SCALE = 4;
    private static final int          DISPLAY_SCALE     = 2;
    private static final RoundingMode ROUNDING          = RoundingMode.HALF_UP;

    private final BigDecimal amount;
    private final Currency   currency;

    private Money(BigDecimal amount, Currency currency) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.amount   = Objects.requireNonNull(amount, "amount must not be null")
                               .setScale(CALCULATION_SCALE, ROUNDING);
    }

    // -- Factories -----------------------------------------------------------
    /** Preferred factory. Use the string form to avoid floating-point errors. */
    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    // -- Arithmetic (all at 4dp calculation scale) ---------------------------
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("cannot divide Money by zero");
        }
        return new Money(amount.divide(divisor, CALCULATION_SCALE, ROUNDING), currency);
    }

    /**
     * Split into {@code parts} equal shares without losing or inventing a penny.
     * Remainder cents are distributed one-per-share from the front, so the shares always
     * sum back to the original at display scale. Fowler's Money-pattern allocation - a
     * naive divide() would silently drop the remainder.
     *
     * <pre>
     * Money.of("10.00", USD).allocate(3)  ->  [3.34, 3.33, 3.33]   // sums to 10.00
     * </pre>
     */
    public java.util.List<Money> allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be a positive integer");
        }
        long totalCents = toDisplayAmount().movePointRight(2).setScale(0).longValueExact();
        long baseCents = totalCents / parts;
        long remainder = totalCents - baseCents * parts;

        java.util.List<Money> shares = new java.util.ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            long shareCents = baseCents + (i < remainder ? 1 : 0);
            shares.add(new Money(BigDecimal.valueOf(shareCents, 2), currency));
        }
        return shares;
    }

    // -- Output --------------------------------------------------------------
    /** 2dp for display, storage, and API output. Not for intermediate calculations. */
    public BigDecimal toDisplayAmount() {
        return amount.setScale(DISPLAY_SCALE, ROUNDING);
    }

    // -- Sign predicates -----------------------------------------------------
    public boolean isPositive() { return amount.compareTo(BigDecimal.ZERO) > 0; }
    public boolean isZero()     { return amount.compareTo(BigDecimal.ZERO) == 0; }
    public boolean isNegative() { return amount.compareTo(BigDecimal.ZERO) < 0; }

    // -- Accessors -----------------------------------------------------------
    /** Returns the amount at 4dp calculation scale. For output use toDisplayAmount(). */
    public BigDecimal getAmount()       { return amount; }
    public Currency   getCurrency()     { return currency; }
    public String     getCurrencyCode() { return currency.getCurrencyCode(); }

    // -- Internals -----------------------------------------------------------
    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "currency mismatch: " + currency.getCurrencyCode()
                    + " vs " + other.currency.getCurrencyCode());
        }
    }

    @Override
    public String toString() {
        return toDisplayAmount().toPlainString() + " " + currency.getCurrencyCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
    }

    @Override
    public int hashCode() {
        // Must be consistent with equals (which uses compareTo, ignoring scale).
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
