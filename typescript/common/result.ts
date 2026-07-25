// Delta: Closing the Specification Gap — Appendix K

export class Result<T> {
  private constructor(
    private readonly _success: boolean,
    private readonly _value?: T,
    readonly errorCode?: string,
    readonly errorMessage?: string,
  ) {}

  static success<T>(value: T): Result<T> {
    if (value === null || value === undefined) {
      throw new Error(
        "success value must not be null/undefined - use Result.voidSuccess() for " +
        "operations that produce no value"
      );
    }
    return new Result(true, value);
  }

  static failure<T>(code: string, message: string): Result<T> {
    return new Result<T>(false, undefined, code, message);
  }

  static voidSuccess(): Result<void> {
    return new Result<void>(true, undefined);
  }

  get isSuccess(): boolean { return this._success; }
  get isFailure(): boolean { return !this._success; }

  get value(): T {
    if (!this._success) {
      throw new Error(`Result is a failure: [${this.errorCode}] ${this.errorMessage}`);
    }
    return this._value!;
  }

  map<U>(fn: (value: T) => U): Result<U> {
    return this._success
      ? Result.success(fn(this._value!))
      : Result.failure(this.errorCode!, this.errorMessage!);
  }

  /**
   * Structural equality. Use this rather than === because Result instances
   * are not reference-equal even when their contents match. C#, Java, and
   * Python implementations get equality from records / dataclasses; in
   * TypeScript it has to be an explicit method.
   */
  equals(other: Result<T>): boolean {
    if (this._success !== other._success) return false;
    if (this._success) return this._value === other._value;
    return this.errorCode === other.errorCode
        && this.errorMessage === other.errorMessage;
  }

  toString(): string {
    return this._success
      ? `Result.success(${this._value})`
      : `Result.failure(${this.errorCode}: ${this.errorMessage})`;
  }
}
