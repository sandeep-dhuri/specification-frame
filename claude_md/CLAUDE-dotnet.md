# CLAUDE.md — .NET 9 Project Configuration
# Place this file in the root of your project repository.
# Claude Code reads this automatically before any agentic task.
# See Chapter 13 for full guidance on CLAUDE.md structure.

## Project Identity

**Company:** [Company Name]
**Domain:** [e.g. Financial Services / Healthcare / E-Commerce]
**Stack:** .NET 9 / C# 13 / ASP.NET Core 9 / EF Core 9 / PostgreSQL 16

## Architecture

**Pattern:** Clean Architecture with CQRS (MediatR 12), vertical slices
**Solution structure:**
```
src/
  Domain/          — Entities, value objects, domain events
  Application/     — Commands, queries, handlers (MediatR), interfaces
  Infrastructure/  — EF Core, repositories, external services
  API/             — Controllers, middleware, DI configuration
tests/
  Unit/
  Integration/
```

**Key types — always use these, never invent alternatives:**
- `Result<T>` and `Result` — from `YourCompany.Common` — for all business rule outcomes
- `Money<TCurrency>` — from `YourCompany.Domain.ValueObjects` — for all monetary values
- Repository pattern — never DbContext outside Infrastructure layer

## Hard Constraints

These constraints are never negotiable. Violating any of them will fail code review.

- **NEVER** use `double` or `float` for monetary amounts
- **NEVER** use `Money<T>(double)` constructor — it is marked `[Obsolete(error:true)]`
- **NEVER** put PHI (name, DOB, NHS number, diagnosis) in log messages
- **NEVER** throw exceptions for business rule failures — return `Result.Failure()`
- **NEVER** use `@Autowired` field injection (this is a .NET project — analogy: no service locator)
- **NEVER** commit EF Core migrations without a corresponding `Down()` method
- **ALWAYS** use `Serilog` structured logging with property syntax: `Log.Information("Payment processed {@PaymentId}", id)`

## Testing Conventions

- Framework: xUnit 2 + FluentAssertions + NSubstitute
- Naming: `[MethodName]_[Scenario]_[ExpectedResult]`
- Coverage requirement: 80% line coverage on Application layer
- Integration tests use TestContainers for PostgreSQL — never SQLite

## Agent Safety Rules

- Do **not** modify `*.csproj` files without explicit instruction
- Do **not** run database migrations without explicit instruction
- Do **not** delete files — create new ones or modify existing ones
- Do **not** commit directly — create files for review
- **Ask** before creating new projects or NuGet package references

## Generating Code

When generating any service or handler, always include:
1. All using statements
2. The complete file — not a snippet
3. `Result<T>` return type for all methods that can fail
4. Constructor injection only
5. XML doc comments on public methods

If anything is ambiguous, state your assumption explicitly before the code:
`// ASSUMPTION: [what you assumed] because [why]`
