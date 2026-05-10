# 2026-05-09 — Add StackDepthFilter

Introduced `StackDepthFilter` and the `StackDepth` singleton marker in
`timber-core`. `Lumberjack.log` now attaches `StackDepth.INSTANCE` to every
`LogEvent` so a registered `StackDepthFilter` fires automatically.

The filter samples the live stack inside `isAllowed` via
`StackWalker.walk(s -> s.limit(maxDepth + 1).count())`, short-circuiting at
the threshold rather than walking to the root. Per-event cost when no
`StackDepthFilter` is registered is one `HashMap.put` for the marker.

Tests: `StackDepthFilterTest` (8 cases) — validation, threshold edges,
recursion-driven allow→deny crossover, `FilterSet` integration, and the
"filter only fires when criterion type is present" contract.

Design notes: `docs/stack-depth-filter.md`.
