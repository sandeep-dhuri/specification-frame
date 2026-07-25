# Security Policy

This repository contains the companion code examples for *Delta: Closing the Specification Gap*
(Sandeep Dhuri · Acuity Press, 2026). The code is **illustrative and educational** — minimal,
pedagogical examples meant to teach specification discipline, not hardened production libraries.
Review, adapt, and test anything here before using it in a real system.

## Reporting a vulnerability

If you find a security issue in this code (or a way an example could mislead a reader into an
insecure pattern), please report it privately rather than opening a public issue:

- **Email:** security@acuity.press
- Include: the file/path, a description, and (if possible) a minimal way to reproduce.

You can expect an acknowledgement within a few days. Confirmed issues will be fixed in the
repository and, where relevant, noted on the book's errata page (acuity.press/delta/errata).

Please do **not** include real secrets, credentials, or production data in a report.

## Scope

- **In scope:** the example code in this repository, and any example that demonstrates an
  insecure pattern without clearly flagging it as such.
- **Out of scope:** the safety of third-party libraries the examples reference (report those
  upstream), and general questions about the book's content (use errata@acuity.press).

## A note on versions

Some examples pin specific language, library, or model versions that were current at the time of
writing. These are treated as **perishable** (see Chapter 11 of the book). An example being
*older* than the current version is not a security issue; an example being *insecure* is.
