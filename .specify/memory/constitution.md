# Constitution

Spec-Kit compatible constitution for AI-assisted development in regulated backends. Derived from the Delta laws ruleset (delta-laws v0.3, MIT), which is the instrument validated in a pre-registered five-model study (doi.org/10.5281/zenodo.22598205: findings fell from 148 to 23, every model improved, none regressed).

Place this file at `.specify/memory/constitution.md` (Spec Kit) or paste it above generation prompts. Every principle is a verifiable sentence. If a principle cannot be checked, it is a wish, not a principle.

## Core principles

### 1. Money is exact
Money arithmetic in decimal.Decimal or integer cents. Round half up only at the final step. Never compare money with floating-point equality.
Rationale: Binary floating point cannot represent most decimal amounts, so drift accumulates and reconciliation fails by cents. (CWE-682, CWE-697. Machine-checkable: yes)

### 2. Time carries its zone
All datetimes are timezone-aware UTC. Naive datetimes are forbidden.
Rationale: Naive timestamps in business logic silently shift across zones and daylight-saving boundaries. (CWE-1339. Machine-checkable: yes)

### 3. Retries are idempotent
Any handler that can be retried (webhooks, payment calls) is idempotent: it requires and checks a caller-supplied idempotency key before applying effects.
Rationale: Retried side effects without deduplication double-charge, double-ship, and double-notify. (no single CWE. Machine-checkable: yes)

### 4. Queries are parameterized
SQL only through parameterized placeholders. String-built queries are forbidden.
Rationale: String-built SQL that reaches execute is injectable regardless of how the input was validated upstream. (CWE-89. Machine-checkable: yes)

### 5. Secrets come from the environment
Credentials and API keys come only from environment variables. Never a literal in source, never logged.
Rationale: Literal credentials in source leak through version control, logs, and error messages. (CWE-798. Machine-checkable: yes)

### 6. Passwords are slow salted hashes
Passwords are stored only as salted, slow hashes (for example hashlib.scrypt or pbkdf2_hmac with high iterations). Never plaintext, never fast unsalted digests.
Rationale: Fast unsalted digests are reversible at scale with commodity hardware. (CWE-916, CWE-328. Machine-checkable: yes)

### 7. Redirects are allow-listed
Redirect targets are validated against an allow-list of known paths or hosts. Never redirect to raw user input.
Rationale: Open redirects turn a trusted domain into a phishing launcher. (CWE-601. Machine-checkable: yes)

### 8. Paths stay inside the base
File paths built from user input are resolved and confirmed to remain inside the intended base directory before use.
Rationale: Unresolved joins with user input escape the intended directory with a handful of dots. (CWE-22. Machine-checkable: yes)

### 9. Validate at the boundary, fail closed
Validate inputs at the boundary. On invalid input, fail closed with a clear error.
Rationale: Inputs that pass the boundary unvalidated become every downstream defect at once. (CWE-20. Machine-checkable: review)

### 10. Never log what must not leak
Never log secrets, PII, PHI, or full card numbers.
Rationale: Logs outlive the request, travel to third parties, and are the first thing an attacker reads. (CWE-532. Machine-checkable: review)

## Governance

1. These principles override style and convenience. A pull request that violates one is not mergeable, regardless of test results.
2. Constraints are checked by deterministic checkers where marked machine-checkable, and by a named reviewer where not.
3. Amendments require a dated entry below with the reason and the checker or review change that enforces it.
4. Ambiguity resolves to the strictest reading consistent with the task. Do not invent scope.

## Amendments

- 2026-09-13: v0.3 initial constitution derived from delta-laws v0.3.

Version: 0.3.0 | Ratified: 2026-09-13 | Source: Delta: Closing the Specification Gap (Dhuri, 2026, doi.org/10.5281/zenodo.21584309)