// Delta companion repo - runnable verification (plain node assertions, no test framework).
//   tsc && node dist/tests/money.test.js
import assert from "node:assert";
import { Money } from "../common/money.js";
import { Result } from "../common/result.js";

// Money
assert.strictEqual(Money.of("19.99", "USD").add(Money.of("2.00", "USD")).toString(), "21.99 USD");
assert.throws(() => Money.of(19.99 as any, "USD"), /not permitted/);
assert.throws(() => Money.of("1", "USD").add(Money.of("1", "EUR")), /mismatch/);
assert.strictEqual(Money.of("10.00", "USD").multiply("3").forOutput(), "30.00");
assert.strictEqual(Money.of("10.00", "USD").divide("3").toDecimal().toFixed(4), "3.3333");
assert.throws(() => Money.of("1", "USD").divide("0"), /zero/);
assert.ok(Money.of("19.99", "USD").equals(Money.of("19.9900", "USD")));

// Result
assert.strictEqual(Result.success(42).value, 42);
assert.throws(() => Result.failure<number>("E", "m").value, /failure/);
assert.strictEqual(Result.success(21).map(x => x * 2).value, 42);
assert.strictEqual(Result.failure<number>("E", "m").map(x => x * 2).errorCode, "E");

console.log("ALL TYPESCRIPT TESTS PASSED (11 assertions)");

// Edge cases (added in production-readiness pass)
import { Money as M2 } from "../common/money.js";
assert.throws(() => M2.of("NaN", "USD"), /finite/);
assert.throws(() => M2.of("Infinity", "USD"), /finite/);
assert.throws(() => M2.of("", "USD"), /finite/);
assert.throws(() => M2.of("abc", "USD"), /finite/);
assert.strictEqual(M2.of("-5.00", "USD").isNegative, true);
assert.throws(() => Result.success(null as any), /null/);
assert.throws(() => Result.success(undefined as any), /null/);
assert.ok(Result.voidSuccess().isSuccess);
console.log("ALL TS EDGE ASSERTIONS PASSED (8 more)");

// allocate (Fowler penny-perfect split)
import { Money as M3 } from "../common/money.js";
const sh = M3.of("10.00","USD").allocate(3);
assert.deepStrictEqual(sh.map(s=>s.forOutput()), ["3.34","3.33","3.33"]);
let allocSum = sh[0]; for (const s of sh.slice(1)) allocSum = allocSum.add(s);
assert.strictEqual(allocSum.forOutput(), "10.00");
assert.throws(()=>M3.of("10.00","USD").allocate(0), /positive/);
console.log("ALLOCATE ASSERTIONS PASSED (3 more)");
