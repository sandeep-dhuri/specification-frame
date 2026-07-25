// Delta: Closing the Specification Gap — Appendix K
// TypeScript Money type using Decimal.js — rejects JS number at runtime.
// JS number is IEEE 754 float64: 0.1 + 0.2 === 0.30000000000000004

import Decimal from "decimal.js";

Decimal.set({ precision: 20, rounding: Decimal.ROUND_HALF_UP });

export class Money {
  private constructor(
    private readonly _amount: Decimal,
    readonly currency: string,
  ) {}

  /**
   * Preferred factory. Pass amount as string or Decimal — never as JS number.
   *
   * ✓  Money.of("19.99", "USD")
   * ✗  Money.of(19.99, "USD")  — throws TypeError at runtime
   *
   * The signature accepts `number` so the runtime guard is reachable; the
   * thrown error tells the caller exactly how to fix the call site.
   */
  static of(amount: string | number | Decimal, currency: string): Money {
    if (typeof amount === "number") {
      throw new TypeError(
        `JS number not permitted for monetary amounts (received ${amount}). ` +
        `Use string: Money.of("${amount.toFixed(2)}", "${currency}")`
      );
    }
    // Reject NaN / Infinity / empty / non-numeric strings before they poison arithmetic.
    let probe: Decimal;
    try {
      probe = new Decimal(amount);
    } catch {
      throw new RangeError(
        `amount must be a finite number (received ${String(amount)})`
      );
    }
    if (!probe.isFinite()) {
      throw new RangeError(
        `amount must be a finite number (received ${String(amount)})`
      );
    }
    return new Money(probe.toDecimalPlaces(4), currency.toUpperCase());
  }

  static zero(currency: string): Money {
    return new Money(new Decimal("0"), currency.toUpperCase());
  }

  // ── Arithmetic ────────────────────────────────────────────────────────────
  add(other: Money): Money {
    this.assertSameCurrency(other);
    return new Money(this._amount.add(other._amount), this.currency);
  }

  subtract(other: Money): Money {
    this.assertSameCurrency(other);
    return new Money(this._amount.sub(other._amount), this.currency);
  }

  multiply(factor: string | Decimal): Money {
    return new Money(this._amount.mul(factor).toDecimalPlaces(4), this.currency);
  }

  divide(divisor: string | Decimal): Money {
    const d = new Decimal(divisor);
    if (d.isZero()) {
      throw new RangeError("cannot divide Money by zero");
    }
    return new Money(this._amount.div(d).toDecimalPlaces(4), this.currency);
  }

  /**
   * Split into `parts` equal shares WITHOUT losing or inventing a penny.
   * Remainder cents are distributed one-per-share from the front, so the parts
   * always sum back to the original at 2dp. Fowler's Money-pattern allocation -
   * a naive divide() would silently drop the remainder.
   *
   *   Money.of("10.00", "USD").allocate(3)  ->  [3.34, 3.33, 3.33]  (sums to 10.00)
   */
  allocate(parts: number): Money[] {
    if (!Number.isInteger(parts) || parts <= 0) {
      throw new RangeError("parts must be a positive integer");
    }
    const totalCents = Number(this.forOutput().replace(".", ""));
    const base = Math.trunc(totalCents / parts);
    const remainder = totalCents - base * parts;
    const shares: Money[] = [];
    for (let i = 0; i < parts; i++) {
      const shareCents = base + (i < remainder ? 1 : 0);
      shares.push(new Money(new Decimal(shareCents).div(new Decimal(100)), this.currency));
    }
    return shares;
  }

  // ── Sign predicates ───────────────────────────────────────────────────────
  get isPositive(): boolean { return this._amount.gt(0); }
  get isZero():     boolean { return this._amount.isZero(); }
  get isNegative(): boolean { return this._amount.lt(0); }

  // ── Comparison ────────────────────────────────────────────────────────────
  equals(other: Money): boolean {
    return this.currency === other.currency
        && this._amount.equals(other._amount);
  }

  // ── Output ────────────────────────────────────────────────────────────────
  /** 2dp string for API responses, display, and database storage. */
  forOutput(): string {
    return this._amount.toFixed(2);
  }

  toDecimal(): Decimal {
    return this._amount;
  }

  toString(): string {
    return `${this.forOutput()} ${this.currency}`;
  }

  // ── Internals ─────────────────────────────────────────────────────────────
  private assertSameCurrency(other: Money): void {
    if (this.currency !== other.currency) {
      throw new Error(
        `Currency mismatch: cannot operate on ${this.currency} and ${other.currency}`
      );
    }
  }
}
