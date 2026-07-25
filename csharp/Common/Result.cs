// Delta: Closing the Specification Gap — Chapter 3 / Appendix B
// Add to your shared library project. Use Result<T> for ALL business rule outcomes.
// Never throw exceptions for business rule failures — always return a Result.

namespace YourCompany.Common;

/// <summary>
/// Typed result for operations that return a value.
/// Success carries a value; failure carries a structured error code and message.
/// </summary>
public sealed class Result<T>
{
    public bool IsSuccess { get; }
    public T? Value { get; }
    public string? ErrorCode { get; }
    public string? ErrorMessage { get; }

    private Result(bool ok, T? value, string? code, string? msg)
        => (IsSuccess, Value, ErrorCode, ErrorMessage) = (ok, value, code, msg);

    public static Result<T> Success(T value) =>
        value is null
            ? throw new ArgumentNullException(nameof(value),
                "Success value must not be null - use Result.Success() (non-generic) or Result<Unit> for void operations")
            : new(true, value, null, null);

    public static Result<T> Failure(string errorCode, string errorMessage) =>
        new(false, default, errorCode, errorMessage);

    /// <summary>Transform the success value without altering failure state.</summary>
    public Result<TOut> Map<TOut>(Func<T, TOut> mapper) =>
        IsSuccess
            ? Result<TOut>.Success(mapper(Value!))
            : Result<TOut>.Failure(ErrorCode!, ErrorMessage!);

    /// <summary>Unwrap or throw — only use when failure is a programming error.</summary>
    public T GetValueOrThrow() => IsSuccess ? Value!
        : throw new InvalidOperationException($"[{ErrorCode}] {ErrorMessage}");

    public override string ToString() =>
        IsSuccess ? $"Success({Value})" : $"Failure({ErrorCode}: {ErrorMessage})";
}

/// <summary>
/// Typed result for commands that produce no value (void operations).
/// </summary>
public sealed class Result
{
    public bool IsSuccess { get; }
    public string? ErrorCode { get; }
    public string? ErrorMessage { get; }

    private Result(bool ok, string? code, string? msg)
        => (IsSuccess, ErrorCode, ErrorMessage) = (ok, code, msg);

    public static Result Success() => new(true, null, null);

    public static Result Failure(string errorCode, string errorMessage) =>
        new(false, errorCode, errorMessage);

    public override string ToString() =>
        IsSuccess ? "Success" : $"Failure({ErrorCode}: {ErrorMessage})";
}

/// <summary>Unit type — used as Result&lt;Unit&gt; for void async operations.</summary>
public readonly struct Unit
{
    public static readonly Unit Value = default;
    public static readonly Result<Unit> SuccessResult =
        Result<Unit>.Success(Value);
}
