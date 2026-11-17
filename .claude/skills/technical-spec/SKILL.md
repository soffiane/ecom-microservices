---
name: technical-spec
description: Turn a product or engineering request into an evidence-based technical specification with scope, requirements, design, risks, and acceptance criteria.
---

# Technical specification

## Goal
Create an actionable technical specification before implementation.

## Workflow
1. Restate the problem and intended outcome.
2. Inspect the repository and existing product behavior relevant to the request.
3. Separate confirmed facts, assumptions, and unanswered questions.
4. Define scope and explicit non-goals.
5. Describe user-facing behavior, functional requirements, non-functional requirements, validation, errors, and permissions.
6. Propose an implementation approach that fits the existing architecture.
7. Identify affected modules, data models, APIs, migrations, integrations, observability, and tests.
8. Consider compatibility, security, privacy, performance, accessibility, and operational risks as relevant.
9. Define testable acceptance criteria.

## Suggested document structure
- Context and problem
- Goals and non-goals
- Current-state findings
- Functional requirements
- Non-functional requirements
- Proposed design
- API/data changes
- Security and failure handling
- Migration and rollout considerations
- Test strategy
- Acceptance criteria
- Open questions and assumptions

## Rules
- Do not implement the feature unless explicitly asked.
- Do not invent business rules. List missing decisions as open questions.
- Keep the design proportionate; avoid unnecessary infrastructure or abstractions.
