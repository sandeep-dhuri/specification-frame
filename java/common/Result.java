// Delta: Closing the Specification Gap — Chapter 3 / Appendix B
package com.yourcompany.common;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Typed result for all business rule outcomes.
 * Use instead of throwing exceptions for expected failure cases.
 *
 * <pre>
 * // ✓ Correct usage
 * return Result.failure("PATIENT_NOT_FOUND", "Patient not found");
 *
 * // ✗ Incorrect — never throw for business rule failures
 * throw new PatientNotFoundException("Patient not found");
 * </pre>
 */
public final class Result<T> {

    private final boolean success;
    private final T value;
    private final String errorCode;
    private final String errorMessage;

    private Result(boolean success, T value, String errorCode, String errorMessage) {
        this.success      = success;
        this.value        = value;
        this.errorCode    = errorCode;
        this.errorMessage = errorMessage;
    }

    // ── Factories ──────────────────────────────────────────────────────────
    public static <T> Result<T> success(T value) {
        Objects.requireNonNull(value, "success value must not be null");
        return new Result<>(true, value, null, null);
    }

    public static <T> Result<T> failure(String errorCode, String errorMessage) {
        Objects.requireNonNull(errorCode,    "errorCode must not be null");
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        return new Result<>(false, null, errorCode, errorMessage);
    }

    /** For void operations — no value on success. */
    public static Result<Void> voidSuccess() {
        return new Result<>(true, null, null, null);
    }

    // ── Accessors ──────────────────────────────────────────────────────────
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }

    public T getValue() {
        if (!success) throw new IllegalStateException(
            "Result is a failure: [" + errorCode + "] " + errorMessage);
        return value;
    }

    public Optional<T> toOptional() {
        return success ? Optional.ofNullable(value) : Optional.empty();
    }

    public String getErrorCode()    { return errorCode; }
    public String getErrorMessage() { return errorMessage; }

    // ── Transform ──────────────────────────────────────────────────────────
    public <U> Result<U> map(Function<T, U> mapper) {
        return success
            ? Result.success(mapper.apply(value))
            : Result.failure(errorCode, errorMessage);
    }

    @Override
    public String toString() {
        return success
            ? "Result.success(" + value + ")"
            : "Result.failure(" + errorCode + ": " + errorMessage + ")";
    }
}
