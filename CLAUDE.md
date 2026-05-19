# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Give Suggestions

- For project improvements after successful task completion
- For tools and agent skills to improve output, reduce time required, or limit token use

## Conventions

- Tabs for indentation in Java sources.
- Javadoc on public types/methods. Comments lead with the *why* (especially for the proxy/cast tricks).
- `@SuppressWarnings({"unchecked","rawtypes"})` is acceptable when the cast is structurally guaranteed by the surrounding contract (see `FilterSet.checkFilter`).

## Documentation Instructions

- Always document architectural and implementation analysis, insights, and plans as .md files in /docs
- Regularly update existing files, especially CLAUDE.md, README.md and /docs/*
- Preserve the history of the project by continuously moving descriptions of modifications to /docs/history
- Limit scope creep of each documentation file by externalizing and linking to non-essential sections in new .md files
- Add inline // FIXME comments where anti-patterns or red flags are detected
- Add inline // TODO comments where future code expansion is expected
