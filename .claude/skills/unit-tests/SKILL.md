---
name: unit-tests
description: Design, add, and run focused automated tests for changed behavior using the repository's existing test framework and conventions.
---

# Unit tests

## Goal
Improve confidence in behavior with maintainable tests that fit the existing project.

## Workflow
1. Detect the language, test framework, test layout, fixtures, mocks, and test commands.
2. Inspect the target implementation and nearby tests before writing anything.
3. Identify observable behavior, normal cases, boundaries, invalid inputs, failures, and relevant regression cases.
4. Present a concise test plan for broad or ambiguous work; proceed for a clearly scoped request.
5. Add tests using existing patterns. Prefer behavior-focused tests over tests coupled to private implementation details.
6. Mock external boundaries where appropriate; do not mock the unit's own logic merely to make tests pass.
7. Run the narrowest relevant test command, then broader checks if practical.
8. Diagnose failures. Do not weaken assertions or change production behavior just to obtain a green test without explaining the reason.

## Quality rules
- Tests must be deterministic and independent.
- Avoid real production services, credentials, or destructive data operations.
- Use synthetic test data.
- Cover meaningful error and boundary behavior.
- Do not claim coverage percentages unless measured.

## Report
List tests added, behaviors covered, commands executed, actual results, and remaining gaps. Do not claim a test passed if it was not run.
