# Delta companion repo - runnable verification tests (stdlib unittest, no dependencies).
#   python -m unittest discover -s tests
import sys, os, unittest
from decimal import Decimal
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from common.money import Money
from common.result import Result


class TestMoney(unittest.TestCase):
    def test_add(self):
        self.assertEqual(str(Money.of("19.99", "USD").add(Money.of("2.00", "USD"))), "21.99 USD")

    def test_float_rejected_constructor(self):
        with self.assertRaises(TypeError):
            Money(amount=19.99, currency="USD")

    def test_float_rejected_factory(self):
        with self.assertRaises(TypeError):
            Money.of(19.99, "USD")  # type: ignore[arg-type]

    def test_currency_mismatch(self):
        with self.assertRaises(ValueError):
            Money.of("1", "USD").add(Money.of("1", "EUR"))

    def test_multiply_4dp(self):
        self.assertEqual(Money.of("10.00", "USD").multiply("3").for_output(), Decimal("30.00"))

    def test_divide_half_up(self):
        self.assertEqual(Money.of("10.00", "USD").divide("3").amount, Decimal("3.3333"))

    def test_divide_by_zero(self):
        with self.assertRaises(ZeroDivisionError):
            Money.of("1", "USD").divide("0")

    def test_trailing_zero_equality(self):
        self.assertEqual(Money.of("19.99", "USD"), Money.of("19.9900", "USD"))

    def test_invalid_currency(self):
        with self.assertRaises(ValueError):
            Money.of("1", "POUND")


class TestResult(unittest.TestCase):
    def test_success(self):
        self.assertTrue(Result.success(42).is_success)
        self.assertEqual(Result.success(42).value, 42)

    def test_failure_value_raises(self):
        with self.assertRaises(ValueError):
            _ = Result.failure("CODE", "msg").value

    def test_map_success(self):
        self.assertEqual(Result.success(21).map(lambda x: x * 2).value, 42)

    def test_map_failure_passthrough(self):
        self.assertEqual(Result.failure("E", "m").map(lambda x: x * 2).error_code, "E")


if __name__ == "__main__":
    unittest.main()


class TestEdgeCases(unittest.TestCase):
    def test_nan_rejected(self):
        with self.assertRaises(ValueError):
            Money.of("NaN", "USD")

    def test_infinity_rejected(self):
        with self.assertRaises(ValueError):
            Money.of("Infinity", "USD")

    def test_empty_string_rejected(self):
        with self.assertRaises(ValueError):
            Money.of("", "USD")

    def test_garbage_rejected(self):
        with self.assertRaises(ValueError):
            Money.of("abc", "USD")

    def test_negative_money(self):
        self.assertTrue(Money.of("-5.00", "USD").is_negative)

    def test_success_none_rejected(self):
        with self.assertRaises(ValueError):
            Result.success(None)

    def test_void_success_allowed(self):
        self.assertTrue(Result.void_success().is_success)


class TestAllocate(unittest.TestCase):
    def test_penny_perfect_split(self):
        shares = Money.of("10.00", "USD").allocate(3)
        self.assertEqual([s.for_output() for s in shares],
                         [Decimal("3.34"), Decimal("3.33"), Decimal("3.33")])

    def test_allocate_sums_back(self):
        shares = Money.of("10.00", "USD").allocate(3)
        total = shares[0]
        for s in shares[1:]:
            total = total.add(s)
        self.assertEqual(total.for_output(), Decimal("10.00"))

    def test_exact_division(self):
        shares = Money.of("9.00", "USD").allocate(3)
        self.assertTrue(all(s.for_output() == Decimal("3.00") for s in shares))

    def test_allocate_invalid_parts(self):
        with self.assertRaises(ValueError):
            Money.of("10.00", "USD").allocate(0)


class TestCriticAttacks(unittest.TestCase):
    """Cases a hostile reviewer would try to disprove the book's claims."""

    def test_decimal_from_float_rejected(self):
        # The headline "no float" claim must survive Decimal(float) sneaking past.
        with self.assertRaises(ValueError):
            Money(amount=Decimal(0.1), currency="USD")

    def test_decimal_from_float_factory_rejected(self):
        with self.assertRaises(ValueError):
            Money.of(Decimal(19.99), "USD")

    def test_legit_high_precision_allowed(self):
        # Genuine string input up to 6dp is fine (quantized to 4dp).
        self.assertEqual(Money.of("19.999999", "USD").for_output(), Decimal("20.00"))

    def test_string_and_int_paths_work(self):
        self.assertEqual(str(Money.of("20", "USD")), "20.00 USD")
        self.assertEqual(str(Money(amount=Decimal("19.99"), currency="USD")), "19.99 USD")
