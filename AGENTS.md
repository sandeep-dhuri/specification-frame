# AGENTS.md — specification-frame companion repository

Operational policy for AI coding agents working in this repository.
(This file practices what the book preaches: Delta, Ch. 11 & 13 — constraint-bearing
instruction files, treated as code.)

## Project overview
Companion code for *Delta: Closing the Specification Gap* (Sandeep Dhuri, 2026).
Four parallel implementations of the same money-safe, Result-typed patterns:
C# (.NET 9), Java 21, Python 3.12, TypeScript (Node 22).

## Build & test commands
- Python:      `cd python && python -m unittest discover -s tests -v`
- TypeScript:  `cd typescript && npm install && npm test`
- Java:        `cd java && javac common/Money.java common/Result.java -d out`
- C#:          standalone value types; project scaffold in the book's Appendix C

## Non-negotiable invariants (from the book's Ten Laws)
- NEVER represent money as float/double/number. String or Decimal in, `Money` type out.
- Money policy in every language: calculation at 4dp, display/storage at 2dp, rounding HALF_UP.
- `Money` arithmetic across different currencies MUST throw. Never auto-convert.
- All error paths return `Result<T>` (or language equivalent). Do not add throwing
  variants to APIs that return Result.
- Tests are the specification. If a change breaks a test, the change is wrong until
  a human says the *test* is wrong.

## Boundaries
- Do not add dependencies beyond decimal.js (TypeScript). The Python implementation
  is stdlib-only by design — keep it that way.
- Do not "modernize" the four implementations apart from each other; they are
  deliberately parallel. A pattern change lands in all four or none.
- Do not edit `LICENSE`, `CITATION.cff`, or ISBN/DOI identifiers.

## Style
- Match the file you are editing. Each language follows its own idioms; none follows another's.
- Comments explain *domain constraints*, not syntax.
