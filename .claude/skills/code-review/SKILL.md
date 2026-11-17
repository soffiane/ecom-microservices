---
name: code-review
description: Review current changes or selected code for correctness, security, maintainability, performance, and regression risks without modifying files.
---

# Code review

## Default mode
Read-only. Do not edit files or automatically fix findings.

## Scope
1. Determine review scope from the user's request; if they say current changes, inspect `git status` and the diff.
2. Read surrounding code and tests so findings are based on behavior, not isolated lines.
3. Check correctness, edge cases, input validation, authorization, data exposure, error handling, concurrency, resource usage, compatibility, and test coverage.
4. Check whether the change follows local architecture and conventions.
5. Ignore purely stylistic preferences unless they violate established project rules.

## Findings
Report only actionable findings. For each:
- Severity: Critical, High, Medium, or Low
- File and line/range
- Concrete failure scenario
- Why it matters
- Suggested remediation

Prioritize exploitable security issues, data loss, crashes, incorrect business behavior, and regressions. Do not inflate severity.

## Final report
- Findings ordered by severity
- Positive observations only when useful
- Areas not reviewed
- Tests or checks run, with actual results

If no findings are identified, say so while noting review scope and limitations. Never claim the code is bug-free.
