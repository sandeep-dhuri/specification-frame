# Delta: Closing the Specification Gap - Appendix K
# Python Money type. Rejects float construction. Stdlib only (no pydantic dependency
# required for a value object). Equivalent to Money<TCurrency> in C# and Money in Java/TS.
#
# Canonical money policy (identical across all four language implementations):
#   - Calculation scale: 4 decimal places
#   - Display / storage scale: 2 decimal places
#   - Rounding: HALF_UP (round half away from zero)
#   - Currency mismatch: raises at runtime (Python/Java/TS); C# prevents at compile time.

from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal, ROUND_HALF_UP

CALCULATION_SCALE = Decimal("0.0001")  # 4 dp
DISPLAY_SCALE = Decimal("0.01")        # 2 dp


def _quantize_calc(value: Decimal) -> Decimal:
    return value.quantize(CALCULATION_SCALE, rounding=ROUND_HALF_UP)


@dataclass(frozen=True)
class Money:
    """
    Immutable monetary value. Rejects float construction at creation time.

    Usage:
        price = Money(amount=Decimal("19.99"), currency="USD")   # OK
        price = Money.of("19.99", "USD")                          # OK
        price = Money(amount=19.99, currency="USD")               # TypeError
    """

    amount: Decimal
    currency: str

    def __post_init__(self) -> None:
        if isinstance(self.amount, float):
            raise TypeError(
                f"float not permitted for monetary amounts (received {self.amount!r}). "
                f'Use Decimal: Money(amount=Decimal("19.99"), currency="USD")'
            )
        cur = str(self.currency).upper().strip()
        if len(cur) != 3 or not cur.isalpha():
            raise ValueError(
                f"currency must be a 3-letter ISO 4217 code (received {self.currency!r})"
            )
        try:
            value = Decimal(str(self.amount))
        except (ArithmeticError, ValueError):
            raise ValueError(f"amount is not a valid decimal number (received {self.amount!r})")
        # Reject NaN and +/-Infinity - they are not valid monetary values.
        if not value.is_finite():
            raise ValueError(f"amount must be a finite number (received {self.amount!r})")
        # Reject float-derived input. A genuine Decimal('0.1') has exponent -1; a Decimal built
        # from a float - Decimal(0.1) - carries IEEE 754 noise (exponent ~ -55). No real currency
        # value needs more than 6 dp, so anything finer is precision corruption sneaking past the
        # isinstance(float) check above. This keeps the book's "no float for money" claim airtight.
        exponent = value.as_tuple().exponent
        if isinstance(exponent, int) and exponent < -6:
            raise ValueError(
                f"amount has more precision than any currency uses (received {self.amount!r}); "
                f"this usually means a float leaked in. Pass a string: Money.of(\"19.99\", \"USD\")"
            )
        object.__setattr__(self, "amount", _quantize_calc(value))
        object.__setattr__(self, "currency", cur)

    @classmethod
    def of(cls, amount: "str | Decimal", currency: str) -> "Money":
        """Preferred factory. Pass amount as a string to avoid float errors."""
        if isinstance(amount, float):
            raise TypeError(
                f'float not permitted (received {amount!r}). Use a string: Money.of("19.99", "USD")'
            )
        return cls(amount=amount, currency=currency)  # validation happens in __post_init__

    @classmethod
    def zero(cls, currency: str) -> "Money":
        return cls(amount=Decimal("0"), currency=currency)

    def add(self, other: "Money") -> "Money":
        self._assert_same_currency(other)
        return Money(amount=self.amount + other.amount, currency=self.currency)

    def subtract(self, other: "Money") -> "Money":
        self._assert_same_currency(other)
        return Money(amount=self.amount - other.amount, currency=self.currency)

    def multiply(self, factor: "str | Decimal") -> "Money":
        if isinstance(factor, float):
            raise TypeError("factor must be Decimal or str, not float")
        return Money(amount=_quantize_calc(self.amount * Decimal(str(factor))),
                     currency=self.currency)

    def divide(self, divisor: "str | Decimal") -> "Money":
        if isinstance(divisor, float):
            raise TypeError("divisor must be Decimal or str, not float")
        d = Decimal(str(divisor))
        if d == 0:
            raise ZeroDivisionError("cannot divide Money by zero")
        return Money(amount=_quantize_calc(self.amount / d), currency=self.currency)

    def allocate(self, parts: int) -> "list[Money]":
        """
        Split this amount into `parts` equal shares WITHOUT losing or inventing a penny.
        The remainder cents are distributed one-per-share from the front, so the parts
        always sum back to the original (at 2dp). This is Fowler's Money-pattern
        allocation - naive divide() would silently drop the remainder.

            Money.of("10.00", "USD").allocate(3) -> [3.34, 3.33, 3.33]  (sums to 10.00)
        """
        if parts <= 0:
            raise ValueError("parts must be a positive integer")
        cents = (self.for_output() * 100).to_integral_value()
        base, remainder = divmod(int(cents), parts)
        shares = []
        for i in range(parts):
            share_cents = base + (1 if i < remainder else 0)
            shares.append(Money(amount=Decimal(share_cents) / 100, currency=self.currency))
        return shares

    @property
    def is_positive(self) -> bool:
        return self.amount > 0

    @property
    def is_zero(self) -> bool:
        return self.amount == 0

    @property
    def is_negative(self) -> bool:
        return self.amount < 0

    def for_output(self) -> Decimal:
        """2dp for API responses, display, and database storage."""
        return self.amount.quantize(DISPLAY_SCALE, rounding=ROUND_HALF_UP)

    def __str__(self) -> str:
        return f"{self.for_output()} {self.currency}"

    def _assert_same_currency(self, other: "Money") -> None:
        if self.currency != other.currency:
            raise ValueError(
                f"currency mismatch: cannot operate on {self.currency} and {other.currency}"
            )
