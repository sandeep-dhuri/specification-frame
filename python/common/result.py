# Delta: Closing the Specification Gap — Appendix K
from __future__ import annotations
from dataclasses import dataclass
from typing import Generic, TypeVar, Optional, Callable

T = TypeVar("T")
U = TypeVar("U")


@dataclass(frozen=True)
class Result(Generic[T]):
    """
    Typed result for all business rule outcomes.
    Use instead of raising exceptions for expected failures.

    Usage:
        return Result.success(patient)
        return Result.failure("PATIENT_NOT_FOUND", "Patient not found")
    """

    _value: Optional[T]
    _error_code: Optional[str]
    _error_message: Optional[str]
    _is_success: bool

    @classmethod
    def success(cls, value: T) -> "Result[T]":
        if value is None:
            raise ValueError(
                "success value must not be None - use Result.void_success() for operations "
                "that produce no value"
            )
        return cls(_value=value, _error_code=None,
                   _error_message=None, _is_success=True)

    @classmethod
    def failure(cls, code: str, message: str) -> "Result[T]":
        return cls(_value=None, _error_code=code,
                   _error_message=message, _is_success=False)

    @classmethod
    def void_success(cls) -> "Result[None]":
        return cls(_value=None, _error_code=None,
                   _error_message=None, _is_success=True)

    @property
    def is_success(self) -> bool:
        return self._is_success

    @property
    def is_failure(self) -> bool:
        return not self._is_success

    @property
    def value(self) -> T:
        if not self._is_success:
            raise ValueError(
                f"Result is a failure: [{self._error_code}] {self._error_message}"
            )
        return self._value  # type: ignore[return-value]

    @property
    def error_code(self) -> Optional[str]:
        return self._error_code

    @property
    def error_message(self) -> Optional[str]:
        return self._error_message

    def map(self, func: Callable[[T], U]) -> "Result[U]":
        if self._is_success:
            return Result.success(func(self.value))
        return Result.failure(self._error_code, self._error_message)  # type: ignore

    def __repr__(self) -> str:
        if self._is_success:
            return f"Result.success({self._value!r})"
        return f"Result.failure({self._error_code!r}: {self._error_message!r})"
