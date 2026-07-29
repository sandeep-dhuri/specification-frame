# SPEC.md — Specification for [feature/task name]

<!-- 
  The Specification Frame: Role · Context · Task · Constraints
  From "Delta: Closing the Specification Gap" by Sandeep Dhuri (free book: https://acuity.press)
  Drop this file next to your AGENTS.md / CLAUDE.md. Fill it before generating. 
  Law 2: A vague specification is not a prompt. It is a request for the model's best guess.
-->

## Role
<!-- The engineering identity and its obligations. Not "helpful assistant" — the professional whose standards apply. -->
You are a [senior backend engineer] working in a [regulated financial services / healthcare / e-commerce] codebase where [PCI-DSS / HIPAA / SOX] obligations apply.

## Context
<!-- Law 4: Context not provided is context invented. Everything unstated below WILL be guessed. -->
- **Stack:** [.NET 9 / Java 21 / Python 3.12 / TypeScript 5.x], [framework + version]
- **Domain types:** amounts use [`Money<Currency>` / `decimal` / `BigDecimal`] — never float/double
- **Error handling:** [Result<T> pattern / exceptions per ADR-nnn] — match existing shape
- **Logging:** [structured, via X; NEVER log: PII, PHI, credentials, full card numbers]
- **Persistence:** [ORM + version; migration tool; naming conventions]
- **Existing conventions:** [test framework + style; folder layout; DI container]

## Task
<!-- One unambiguous sentence. If you can't restate it in one sentence, the spec is incomplete. -->
Implement [ ... ] such that [observable outcome].

**Out of scope:** [what NOT to touch — adjacent modules, refactors, dependency upgrades]

## Constraints
<!-- Law 3: Output quality is bounded by constraint quality. Missing one constraint = one open failure mode. 
     Delete lines that don't apply; every line you delete is a decision, not an omission. -->
- [ ] Money is [decimal type]; no binary floating point on amounts (Law 5)
- [ ] External-facing handlers are **idempotent under retry**; document the idempotency key
- [ ] No secrets/PII/PHI in code, comments, fixtures, or logs
- [ ] User-controlled text reaching any LLM call is **structurally isolated** — never concatenated into instructions (Law 6)
- [ ] Input validation at the boundary: [reject / normalize] on [rules]
- [ ] Concurrency: [optimistic locking / transaction isolation level] for [entities]
- [ ] Every behavioral change ships with tests in the project's existing style; edge cases: [empty, null, max, duplicate, retry]
- [ ] Performance bound: [p95 < X ms / batch of N in T]
- [ ] If any requirement above is unstated or ambiguous: **name the gap and ask — do not guess** (Law 9)

## Acceptance
<!-- How a reviewer verifies the delta between asked and delivered is zero. -->
- [ ] [Observable check 1]
- [ ] [Observable check 2]
- [ ] Tests pass: `[command]`

---
*Template: CC BY 4.0 · the Specification Frame technique described by Sandeep Dhuri (2026) — "Delta: Closing the Specification Gap", Acuity Press, free at https://acuity.press · The Ten Laws: https://acuity.press/laws/*
