# Claude Code Developer Toolkit

A reusable, technology-agnostic starter kit for Claude Code. It includes project guidance, six focused Skills, and three specialized subagents.

## Contents

```text
.
├── CLAUDE.md
├── README.md
└── .claude/
    ├── agents/
    │   ├── software-architect.md
    │   ├── code-reviewer.md
    │   └── test-engineer.md
    └── skills/
        ├── api-documentation/SKILL.md
        ├── architecture-analysis/SKILL.md
        ├── code-review/SKILL.md
        ├── refactoring/SKILL.md
        ├── technical-spec/SKILL.md
        └── unit-tests/SKILL.md
```

## Install in a project

1. Back up any existing `CLAUDE.md` and `.claude/` directory.
2. Copy `CLAUDE.md` and the `.claude/` directory into the root of your repository.
3. If those files already exist, merge their contents instead of overwriting them.
4. Open Claude Code from the repository root:

   ```bash
   cd path/to/your-project
   claude
   ```

5. Ask Claude to inspect the repository and summarize the detected stack and conventions.
6. Try a Skill with its slash command, for example `/architecture-analysis` or `/code-review`.

Project Skills live in `.claude/skills/<skill-name>/SKILL.md`. Project subagents live in `.claude/agents/<agent-name>.md`.

## Suggested first prompts

### Understand the repository

> Inspect this repository without modifying files. Detect the language, frameworks, build and test commands, architecture, entry points, and conventions. Give me a concise technical overview and list uncertainties.

### Generate documentation

> Use `/api-documentation` to inspect the actual API implementation and update API documentation. Do not invent endpoints or schemas. List any information that cannot be verified from the code.

### Review a change

> Review the current Git diff using `/code-review`. Do not edit files. Prioritize correctness, security, regressions, and missing tests. Report findings with file and line references.

### Add tests

> Use `/unit-tests` to identify important untested behavior in the service I specify. Propose a test plan first, then implement tests using the repository's existing test framework.

### Plan a feature

> Use `/technical-spec` to turn this feature request into a technical specification. Inspect the existing code first, identify assumptions and open questions, and do not implement it yet.

## What each component does

### CLAUDE.md
Persistent project context: commands, architecture facts, conventions, safety rules, and verification expectations. Keep it concise and project-specific.

### Skills
Reusable procedures invoked as slash commands or selected automatically when relevant:
- `/api-documentation`
- `/architecture-analysis`
- `/code-review`
- `/refactoring`
- `/technical-spec`
- `/unit-tests`

### Subagents
Specialized assistants Claude can delegate bounded tasks to:
- `software-architect`: architecture and design analysis.
- `code-reviewer`: independent, read-only review.
- `test-engineer`: test strategy and test implementation.

## Customize for your stack

This kit intentionally does not assume a particular language or framework. After installation, ask Claude to inspect the repository and propose project-specific additions to `CLAUDE.md`, including:
- exact build, test, lint, and formatting commands;
- architectural boundaries and naming conventions;
- database and migration practices;
- API conventions and error formats;
- security and deployment constraints.

Review those proposed changes before accepting them.

## Safety notes

- Review all generated changes before committing.
- Keep secrets out of prompts, Skills, and `CLAUDE.md`.
- Review third-party Skills and scripts before using them.
- Keep deployments, production changes, destructive operations, and external side effects behind explicit approval.
- Use Git branches and commits so changes are easy to inspect and revert.

## Official references

- Claude Code documentation: https://code.claude.com/docs
- Skills: https://code.claude.com/docs/en/skills
- Subagents: https://code.claude.com/docs/en/sub-agents
- Memory / CLAUDE.md: https://code.claude.com/docs/en/memory
- Security: https://code.claude.com/docs/en/security
