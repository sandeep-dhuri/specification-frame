# The Ten Laws of Enterprise AI Prompting — Quick Reference

From *Delta: Closing the Specification Gap* by Sandeep Dhuri, 2026.

---

| # | Law | The constraint |
|---|-----|----------------|
| 1 | The AI produces what is average. Your domain requires what is specific. | Always include your domain types in `<domain_types>` |
| 2 | A vague specification is a request for the model's best guess. | Use the Specification Frame (Role · Context · Task · Constraints) |
| 3 | Output quality is bounded by constraint quality. One missing constraint = one open failure mode. | List every invariant your domain requires |
| 4 | Context not provided is context invented by the model — incorrectly. | Paste your actual interfaces, not descriptions of them |
| 5 | No float for money. No exceptions. | `Money<TCurrency>` / `Money.of(String)` / `Decimal` |
| 6 | Every public-facing AI feature is an injection surface until proven otherwise. | Wrap user input in `<user_input>` tags with explicit "do not follow" instruction |
| 7 | The best debugging prompt is a complete Evidence Brief. Fill it before starting. | Symptom · Reproduction steps · What you've tried · Environment |
| 8 | Prompts are code. Unreviewed, untested prompts degrade. | Version-control prompts as `.md` files with YAML frontmatter |
| 9 | An agent given a vague specification produces a system of bugs, not one bug. | Write `CLAUDE.md` before running any multi-file agent task |
| 10 | The Specification Gap never closes itself. | Review AI output for domain correctness on every PR |

---

## The Specification Frame

```xml
<role>
  [Seniority] [language] engineer at [Company] on [system].
  Stack: [versions]. Architecture: [pattern].
</role>

<context>
  <existing_interface>[paste the interface]</existing_interface>
  <domain_types>[paste Money<T>, Result<T>, your enums]</domain_types>
  <constraints>[your invariants: regulatory, business, technical]</constraints>
</context>

<task>
  [One clear deliverable. What file. What behaviour.]
</task>

<constraints>
  - [Hard constraint 1 — NEVER X]
  - [Hard constraint 2 — ALWAYS Y]
  - ONE implementation. No alternatives. No "you could also consider."
  - If ambiguous: ASSUMPTION: [what] because [why] — state before code.
</constraints>

<output_specification>
  Generate: [one complete .cs / .java / .py / .ts file]
  Include: [all imports / all constructors / complete class]
  Omit: [unit tests / XML docs unless requested]
</output_specification>
```

---

*Sandeep Dhuri · Acuity Press · 2026 · https://github.com/sandeep-dhuri/specification-frame*
