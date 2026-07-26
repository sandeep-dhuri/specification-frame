# Delta: Closing the Specification Gap — Code Repository

Companion repository for *Delta: Closing the Specification Gap* by Sandeep Dhuri (Acuity Press, 2026).

All compilable code examples, prompt templates, and CLAUDE.md configuration files from the book.

---

[![CI](https://github.com/sandeep-dhuri/specification-frame/actions/workflows/ci.yml/badge.svg)](https://github.com/sandeep-dhuri/specification-frame/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-amber.svg)](LICENSE)
![.NET 9](https://img.shields.io/badge/.NET-9-512BD4)
![Java 21](https://img.shields.io/badge/Java-21-007396)
![Python 3.12](https://img.shields.io/badge/Python-3.12-3776AB)
![TypeScript 5+](https://img.shields.io/badge/TypeScript-5%2B-3178C6)
![PRs welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)

> Companion code for the book *Delta: Closing the Specification Gap*. The examples are **illustrative and educational** — minimal and pedagogical, not drop-in production libraries. Read [SECURITY.md](SECURITY.md) and [CONTRIBUTING.md](CONTRIBUTING.md) before using or contributing.

## Quick start (60 seconds, no project setup)

Clone, then run the dependency-free suites:

```bash
git clone https://github.com/sandeep-dhuri/specification-frame.git
cd specification-frame

# Python 3.12 — runs against the standard library only
cd python && python -m unittest discover -s tests -v && cd ..

# TypeScript 5+ — needs Node 18+
cd typescript && npm install --no-save typescript decimal.js \
  && npx tsc --module nodenext --target es2022 --outDir dist common/*.ts tests/*.ts \
  && node dist/tests/money.test.js && cd ..
```

The **C#** and **Java** sources are designed to be dropped into your own project (see *Build and run* below); their tests run in your host build, which is why CI runs only the Python and TypeScript suites.

---

## What is in this repository

| Directory | Contents | Book chapter |
|-----------|----------|--------------|
| `csharp/` | C# .NET 9 — `Result<T>`, `Money<T>`, `SecurePromptBuilder` | Ch 3, 4, 15 / App B |
| `java/` | Java 21 — `Result<T>`, `Money`, idempotent Stripe webhook | Ch 3, 4 / App B |
| `python/` | Python 3.12 — `Money` (stdlib `dataclass` + `Decimal`), `Result` | App K |
| `typescript/` | TypeScript — `Money` (Decimal.js), `Result` | App K |
| `prompts/` | `.md` prompt files with YAML frontmatter | Ch 11 |
| `claude_md/` | CLAUDE.md templates for Claude Code | Ch 13 |
| `docs/` | Ten Laws quick-reference card | Chs 1–17 |

---

## Start here

Copy these two files into your project first. They underpin every recipe in the book.

**C# / .NET:** `csharp/Common/Result.cs`, `csharp/Domain/ValueObjects/Money.cs`

**Java:** `java/common/Result.java`, `java/common/Money.java`

**Python:** `python/common/result.py`, `python/common/money.py`

**TypeScript:** `typescript/common/result.ts`, `typescript/common/money.ts`

---

## A working hello world

The fastest way to confirm the files dropped into your project correctly: try a tiny program that uses both `Money` and `Result` together.

### C# (.NET 9 / C# 13)

```csharp
using YourCompany.Common;
using YourCompany.Domain.ValueObjects;

Result<Money<USD>> CalculateTotal(Money<USD> price, Money<USD> tax)
{
    if (price.IsNegative)
        return Result<Money<USD>>.Failure("INVALID_PRICE", "Price must be non-negative");
    return Result<Money<USD>>.Success(price.Add(tax));
}

var price = new Money<USD>(19.99m);
var tax   = new Money<USD>(2.00m);
var total = CalculateTotal(price, tax);

Console.WriteLine(total.IsSuccess ? $"Total: {total.Value}" : $"Error: {total.ErrorMessage}");
// → Total: 21.99
```

### Java (JDK 21)

```java
import com.yourcompany.common.Money;
import com.yourcompany.common.Result;
import java.util.Currency;

Result<Money> calculateTotal(Money price, Money tax) {
    if (price.isNegative())
        return Result.failure("INVALID_PRICE", "Price must be non-negative");
    return Result.success(price.add(tax));
}

var usd   = Currency.getInstance("USD");
var price = Money.of("19.99", usd);
var tax   = Money.of("2.00", usd);
var total = calculateTotal(price, tax);

System.out.println(total.isSuccess() ? "Total: " + total.getValue() : "Error: " + total.getErrorMessage());
// → Total: 21.99 USD
```

### Python 3.12

```python
from decimal import Decimal
from common.money import Money
from common.result import Result

def calculate_total(price: Money, tax: Money) -> Result[Money]:
    if price.is_negative:
        return Result.failure("INVALID_PRICE", "Price must be non-negative")
    return Result.success(price.add(tax))

price = Money(amount=Decimal("19.99"), currency="USD")
tax   = Money(amount=Decimal("2.00"),  currency="USD")
total = calculate_total(price, tax)

print(f"Total: {total.value}" if total.is_success else f"Error: {total.error_message}")
# → Total: 21.99 USD
```

### TypeScript

```typescript
import { Money } from "./common/money.js";
import { Result } from "./common/result.js";

function calculateTotal(price: Money, tax: Money): Result<Money> {
  if (price.isNegative)
    return Result.failure<Money>("INVALID_PRICE", "Price must be non-negative");
  return Result.success(price.add(tax));
}

const price = Money.of("19.99", "USD");
const tax   = Money.of("2.00",  "USD");
const total = calculateTotal(price, tax);

console.log(total.isSuccess ? `Total: ${total.value}` : `Error: ${total.errorMessage}`);
// → Total: 21.99 USD
```

All four print the same output: `Total: 21.99 USD` (or `Total: 21.99` for C# without currency in `toString`).

### Money policy (identical across all four languages)

Every `Money` implementation uses the same arithmetic contract, so results match across languages:

- **Calculation scale:** 4 decimal places (intermediate arithmetic)
- **Display / storage scale:** 2 decimal places (`toDisplayAmount` / `forOutput` / `for_output` / `ToString`)
- **Rounding:** HALF_UP (C# uses `MidpointRounding.AwayFromZero`, which is equivalent for the values money uses)
- **Currency safety:** C# enforces it at *compile time* via the `Money<TCurrency>` type parameter; Java, Python, and TypeScript check at *runtime* and throw on mismatch. Both are deliberate — the C# design catches the error earlier; the others work without generics.

### `allocate(parts)` — penny-perfect division

Every `Money` type includes `allocate(n)` (Fowler's Money-pattern split). It divides an amount into `n` equal shares **without losing or inventing a penny** — the remainder cents are distributed one per share, so the parts always sum back to the original:

```
Money.of("10.00", "USD").allocate(3)  ->  [3.34, 3.33, 3.33]   (sums to 10.00)
```

A naive `divide(3)` would give `3.33` three times and silently lose a penny — exactly the kind of Specification-Gap bug the book is about.

### A note on rounding mode (read before you adopt this in regulated finance)

These implementations use **HALF_UP** (round half away from zero). This is a deliberate, common
choice, but it is **not the only correct one**, and the right answer depends on your domain:

- **HALF_UP** — simple, predictable, what most people expect ("0.5 rounds up"). Fine for most
  e-commerce, tax, and general business arithmetic.
- **HALF_EVEN (banker's rounding)** — the IEEE 754 default and the expected standard in much of
  banking and accounting, because it removes the slight upward bias HALF_UP introduces when you
  round many values in aggregate. If you are in regulated finance, **check your jurisdiction's
  and your accounting team's requirement** — you may be required to use HALF_EVEN.

The policy is defined in one place per file (the `ROUNDING` / `Rounding` constant). To change it,
change that constant; all arithmetic and display honour it. The book's point is not that HALF_UP
is universally right — it is that the rounding mode must be an *explicit, single, documented
decision* rather than whatever each call site happens to default to.

### A note on the "no float" guarantee

The float rejection is enforced at every entry point: the `number`/`float` type is rejected
outright, and (in Python) a `Decimal` that was built from a float — e.g. `Decimal(0.1)`, which
already carries IEEE 754 noise like `0.1000000000000000055…` — is also rejected, because no real
currency value needs more than six decimal places. The only safe way in is a **string** or an
**integer**: `Money.of("19.99", "USD")`.

---

## Continuous integration

A GitHub Actions workflow (`.github/workflows/ci.yml`) runs the dependency-free
verification tests on every push and pull request: the Python suite (stdlib
`unittest`, 28 tests) and the TypeScript suite (`tsc` + `node`). The C# and Java
sources are designed to be dropped into your own project (see below), so their
tests run in your host build rather than in this workflow.

## Build and run

### C# (.NET 9 / C# 13)

```bash
cd csharp
dotnet build
dotnet test          # xUnit + FluentAssertions (see tests/)
```

Place the files into a class library project targeting `net9.0`. The `SecurePromptBuilder` class uses raw string literals (C# 11+) and requires no external NuGet packages.

### Java (JDK 21)

```bash
cd java
javac -d build common/*.java
```

`StripeWebhookController.java` additionally requires:

- Spring Boot 3.3 (`spring-boot-starter-web`)
- Stripe Java SDK 26+ (`com.stripe:stripe-java`)
- SLF4J 2.x (transitive via Spring Boot)

The controller depends on `PaymentService` and `IdempotencyRepository` interfaces which are included in this repository as stubs (`java/payments/PaymentService.java`, `java/payments/IdempotencyRepository.java`) — provide your own implementations in your application module.

### Python 3.12

No third-party dependencies — `Money` and `Result` use only the standard library
(`dataclasses`, `decimal`).

```bash
cd python
python -c "from common.money import Money; print(Money.of('19.99', 'USD'))"
python -m unittest discover -s tests -v
```

`Money` is a frozen `dataclass`; it rejects `float` construction at creation time
(`__post_init__`) and holds amounts at 4dp calculation scale.

### TypeScript 5+

```bash
cd typescript
npm install decimal.js
tsc --strict --target ES2022 --module ES2022 --moduleResolution bundler --outDir dist common/*.ts
```

The TypeScript files are written for `--strict` mode (the recommended default). If you place the files in a subdirectory, set `rootDir` in `tsconfig.json` to silence the TS6 layout warning.

---

## License

MIT — see [LICENSE](LICENSE).

You are free to use, modify, and redistribute the code for any purpose. Attribution appreciated:

> *Adapted from* Delta: Closing the Specification Gap *by Sandeep Dhuri (2026).*

Found an error? [Open an issue](../../issues) or email [errata@acuity.press].

---

*Sandeep Dhuri · Sandeep Dhuri · 2026*
