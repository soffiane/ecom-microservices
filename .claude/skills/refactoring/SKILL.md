---
name: refactoring
description: Safely improve code structure and maintainability while preserving externally observable behavior and validating changes with tests.
---

# Refactoring

## Goal
Improve internal design without changing behavior unless the user explicitly requests a behavior change.

## Workflow
1. Inspect the target code, callers, tests, and local conventions.
2. Identify a specific maintainability problem and explain why the proposed refactor helps.
3. Check whether tests adequately characterize current behavior. If not, recommend or add characterization tests first.
4. Propose a small sequence of changes for non-trivial refactors.
5. Refactor incrementally, keeping each step easy to review.
6. Run relevant tests, formatters, linters, and build checks.
7. Review the final diff for accidental behavior changes and unrelated edits.

## Constraints
- Preserve public interfaces and serialized formats unless authorized to change them.
- Avoid combining refactoring with unrelated feature work.
- Avoid broad rewrites and speculative abstractions.
- Do not change database schemas, migrations, dependencies, or deployment configuration without explaining the need and obtaining approval.
- Do not suppress failing tests or weaken validation to make the refactor appear successful.

## Report
Explain the original problem, key structural changes, behavior-preservation evidence, checks run, and remaining risks.
