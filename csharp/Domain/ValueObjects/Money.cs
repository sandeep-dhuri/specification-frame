// Delta: Closing the Specification Gap - Chapter 3 / Appendix B
//
// The [Obsolete(error:true)] constructors make constructing Money from a double or
// float a COMPILE-TIME ERROR - the single most common AI code-generation money bug.
//
// CURRENCY SAFETY MODEL (differs deliberately from Java/Python/TS):
//   The C# implementation enforces currency safety at COMPILE TIME via the TCurrency
//   type parameter - mixing Money<USD> and Money<EUR> simply will not compile, so no
//   runtime assertSameCurrency check is needed (or possible). The Java, Python, and
//   TypeScript implementations carry the currency as a value and check it at runtime.
//   Both are valid; this one catches the error earlier.
//
// CANONICAL MONEY POLICY (identical across all four language implementations):
//   - Calculation scale: 4 decimal places
//   - Display / storage scale: 2 decimal places
//   - Rounding: MidpointRounding.AwayFromZero (== HALF_UP for the values money uses)

using System.Collections.Generic;

namespace YourCompany.Domain.ValueObjects;

// -- Currency marker structs ---------------------------------------------------
// Add your currencies here. The type parameter prevents mixing USD and EUR at compile time.
public readonly struct USD { }
public readonly struct EUR { }
public readonly struct GBP { }
public readonly struct AUD { }

// -- Money<TCurrency> ----------------------------------------------------------
/// <summary>
/// Immutable monetary value with compile-time currency type safety.
///
/// USAGE:
///   var price = new Money&lt;USD&gt;(19.99m);    // correct (decimal literal)
///   var price = new Money&lt;USD&gt;(19.99);     // COMPILE ERROR (Obsolete error:true)
///   var price = new Money&lt;USD&gt;(19.99f);    // COMPILE ERROR (Obsolete error:true)
///
/// All arithmetic is computed at 4dp; use Round() or ToString() for 2dp display output.
/// </summary>
public sealed record Money<TCurrency>
    where TCurrency : struct
{
    private const int CalculationScale = 4;
    private const int DisplayScale = 2;
    private const MidpointRounding Rounding = MidpointRounding.AwayFromZero;

    /// <summary>Amount, always held at 4dp calculation scale.</summary>
    public decimal Amount { get; }

    public Money(decimal amount) =>
        Amount = Math.Round(amount, CalculationScale, Rounding);

    // -- Poison constructors - compile-time prevention -------------------------
    [Obsolete("NEVER construct Money<T> from double. Use a decimal literal: new Money<USD>(19.99m)", error: true)]
    public Money(double amount) : this((decimal)amount) { }

    [Obsolete("NEVER construct Money<T> from float. Use a decimal literal: new Money<USD>(19.99m)", error: true)]
    public Money(float amount) : this((decimal)amount) { }

    // -- Arithmetic (all at 4dp calculation scale) -----------------------------
    public Money<TCurrency> Add(Money<TCurrency> other) =>
        new(Amount + other.Amount);

    public Money<TCurrency> Subtract(Money<TCurrency> other) =>
        new(Amount - other.Amount);

    public Money<TCurrency> Multiply(decimal factor) =>
        new(Amount * factor);

    public Money<TCurrency> Divide(decimal divisor) =>
        divisor == 0m
            ? throw new DivideByZeroException("cannot divide Money by zero")
            : new(Amount / divisor);

    /// <summary>
    /// Split into <paramref name="parts"/> equal shares without losing or inventing a penny.
    /// Remainder cents are distributed one-per-share from the front, so the shares always
    /// sum back to the original at display scale. Fowler's Money-pattern allocation - a
    /// naive Divide() would silently drop the remainder.
    /// <code>new Money&lt;USD&gt;(10.00m).Allocate(3) =&gt; [3.34, 3.33, 3.33]</code>
    /// </summary>
    public IReadOnlyList<Money<TCurrency>> Allocate(int parts)
    {
        if (parts <= 0)
            throw new ArgumentOutOfRangeException(nameof(parts), "parts must be a positive integer");

        var totalCents = (long)decimal.Round(ForOutput() * 100m, 0, Rounding);
        var baseCents = totalCents / parts;
        var remainder = (int)(totalCents - baseCents * parts);

        var shares = new Money<TCurrency>[parts];
        for (var i = 0; i < parts; i++)
        {
            var shareCents = baseCents + (i < remainder ? 1 : 0);
            shares[i] = new Money<TCurrency>(shareCents / 100m);
        }
        return shares;
    }

    // -- Sign predicates -------------------------------------------------------
    public bool IsPositive => Amount > 0m;
    public bool IsZero     => Amount == 0m;
    public bool IsNegative => Amount < 0m;

    // -- Factories -------------------------------------------------------------
    public static Money<TCurrency> Zero => new(0m);
    public static Money<TCurrency> Of(decimal amount) => new(amount);

    // -- Output ----------------------------------------------------------------
    /// <summary>Amount rounded to display scale (2dp). Use for output and storage, not calculation.</summary>
    public decimal ForOutput() => Math.Round(Amount, DisplayScale, Rounding);

    /// <summary>2dp display string. Currency code is not shown - it lives only in the type parameter.</summary>
    public override string ToString() => ForOutput().ToString($"F{DisplayScale}");
}
