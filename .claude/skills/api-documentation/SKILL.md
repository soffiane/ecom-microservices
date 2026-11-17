---
name: api-documentation
description: Inspect implemented APIs and create or update accurate REST, GraphQL, or RPC documentation from source code, schemas, and tests.
---

# API documentation

## Goal
Produce developer-facing API documentation that reflects the implementation, not assumptions.

## Workflow
1. Detect the API style and framework from the repository.
2. Inspect route declarations, controllers/handlers, request and response models, validation, authentication, authorization, error handling, and relevant tests.
3. Trace important endpoints into service and persistence layers when needed to verify behavior.
4. Identify versioning, pagination, filtering, sorting, rate limits, idempotency, and side effects where present.
5. Document only facts supported by code, tests, schemas, or existing authoritative project documentation.
6. Mark unclear or undocumented behavior as `Needs confirmation`; do not guess.
7. Prefer updating the existing documentation format and location. If none exists, propose a suitable `docs/api/` structure before creating many files.

## Endpoint template
For each endpoint, document:
- Method and path
- Purpose
- Authentication and authorization
- Path, query, and header parameters
- Request body and validation constraints
- Success response and representative example
- Error responses and status codes
- Side effects and relevant notes

## Quality checks
- Cross-check documented paths and methods against route definitions.
- Cross-check schemas against DTOs, serializers, or validation code.
- Ensure examples do not contain real credentials or personal data.
- Avoid claiming an endpoint exists if it is only planned or mocked.
- State which files were inspected and which uncertainties remain.

## Output
Provide a short summary of files created or updated, coverage, and unresolved questions. Do not modify application code unless explicitly requested.
