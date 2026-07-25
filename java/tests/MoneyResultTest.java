// Delta companion repo - JUnit 5 verification tests.
//   (Place under src/test/java; run with: mvn test  or  gradle test)
package com.yourcompany.common;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import static org.junit.jupiter.api.Assertions.*;

class MoneyResultTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void addSameCurrency() {
        assertEquals("21.99 USD",
            Money.of("19.99", USD).add(Money.of("2.00", USD)).toString());
    }

    @Test
    void multiplyFourDp() {
        assertEquals(new BigDecimal("30.00"),
            Money.of("10.00", USD).multiply(new BigDecimal("3")).toDisplayAmount());
    }

    @Test
    void divideHalfUp() {
        assertEquals(new BigDecimal("3.3333"),
            Money.of("10.00", USD).divide(new BigDecimal("3")).getAmount());
    }

    @Test
    void divideByZeroThrows() {
        assertThrows(ArithmeticException.class,
            () -> Money.of("1", USD).divide(BigDecimal.ZERO));
    }

    @Test
    void currencyMismatchThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Money.of("1", USD).add(Money.of("1", EUR)));
    }

    @Test
    void equalityIgnoresScale() {
        assertEquals(Money.of("19.99", USD), Money.of("19.9900", USD));
    }

    @Test
    void allocateSplitsWithoutLosingAPenny() {
        var shares = Money.of("10.00", USD).allocate(3);
        assertEquals(new java.math.BigDecimal("3.34"), shares.get(0).toDisplayAmount());
        assertEquals(new java.math.BigDecimal("3.33"), shares.get(1).toDisplayAmount());
        assertEquals(new java.math.BigDecimal("3.33"), shares.get(2).toDisplayAmount());
        var total = shares.stream().reduce(Money.zero(USD), Money::add);
        assertEquals(new java.math.BigDecimal("10.00"), total.toDisplayAmount());
    }

    @Test
    void allocateInvalidPartsThrows() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10.00", USD).allocate(0));
    }

    @Test
    void resultSuccessCarriesValue() {
        Result<Integer> r = Result.success(42);
        assertTrue(r.isSuccess());
        assertEquals(42, r.getValue());
    }

    @Test
    void resultFailureGetValueThrows() {
        Result<Integer> r = Result.failure("CODE", "msg");
        assertThrows(IllegalStateException.class, r::getValue);
    }

    @Test
    void resultMapTransforms() {
        assertEquals(42, Result.success(21).map(x -> x * 2).getValue());
    }

    @Test
    void resultMapFailurePassesThrough() {
        assertEquals("E", Result.<Integer>failure("E", "m").map(x -> x * 2).getErrorCode());
    }

    @Test
    void resultSuccessNullThrows() {
        assertThrows(NullPointerException.class, () -> Result.success(null));
    }

    @Test
    void resultVoidSuccessAllowed() {
        assertTrue(Result.voidSuccess().isSuccess());
    }
}
