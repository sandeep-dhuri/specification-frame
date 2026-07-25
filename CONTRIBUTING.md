# Contributing

Thanks for your interest. This repository accompanies the book *Delta: Closing the Specification
Gap*. Its goal is to be a faithful, runnable companion to the examples in the book — not a
general-purpose library — so contributions are scoped accordingly.

## What's welcome

- **Corrections**: a bug in an example, a typo, a broken link, an example that no longer compiles
  or runs against the versions noted.
- **Clarity**: making an example easier to follow without changing what it teaches.
- **Version notes**: flagging where a pinned language/library/model version has moved on (the code
  is intentionally version-pinned; see the book's Chapter 11 on treating versions as perishable).

## What's out of scope

- Large new features or rewrites that diverge from the book's text (the code must match the book).
- Adding new dependencies, or "modernising" an example in a way that changes its teaching point.
- Style-only churn.

## How to contribute

1. Open an issue first describing the change, so we can confirm it fits the book.
2. For code changes, open a pull request against `main`. Keep it small and focused.
3. Make sure the test suite still passes (`see README` for per-language commands) and CI is green.
4. By contributing, you agree your contribution is licensed under the repository's MIT License.

## Reporting security issues

Do **not** open a public issue for security problems — see `SECURITY.md`.

## Errata for the book itself

Found an error in the *book* (not the code)? Email **errata@acuity.press** with the chapter, page,
and a short description. Confirmed corrections appear at acuity.press/delta/errata.
