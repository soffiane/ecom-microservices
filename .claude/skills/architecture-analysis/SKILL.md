---
name: architecture-analysis
description: Analyze a repository's architecture, dependencies, modules, data flows, and design boundaries; produce evidence-based technical documentation and diagrams.
---

# Architecture analysis

## Goal
Explain how the system is actually structured and how its important runtime and data flows work.

## Workflow
1. Map top-level directories and identify entry points, deployable units, build files, and runtime configuration.
2. Detect languages, frameworks, persistence technologies, messaging systems, external integrations, and test tools.
3. Identify architectural style only when supported by evidence; distinguish intended architecture from observed implementation.
4. Trace key flows end-to-end: request/event, validation, business logic, persistence, integrations, and response.
5. Identify module responsibilities, dependency directions, shared components, and potential coupling.
6. Note security boundaries, configuration sources, and operational dependencies without exposing secrets.
7. Generate Mermaid diagrams only when they clarify the system. Ensure diagram nodes and edges reflect verified relationships.

## Deliverable
Produce a structured report:
- Executive summary
- Technology inventory
- Repository and module map
- Application entry points
- Main runtime/data flows
- External dependencies and integrations
- Persistence model overview
- Build, test, and deployment overview (if discoverable)
- Architectural strengths and risks
- Unknowns and questions

## Rules
- Do not modify source code.
- Cite paths and symbols as evidence.
- Do not infer runtime behavior from filenames alone.
- Clearly label hypotheses and recommend how to verify them.
