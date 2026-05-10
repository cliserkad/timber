# StackDepthFilter

A `Filter` that suppresses log events whose call site sits deeper than a configured
maximum number of stack frames. Useful for cutting noise from helper-of-helper code
while keeping top-level orchestration messages.

## Public types

- `xyz.cliserkad.timber.StackDepth` — singleton marker (`StackDepth.INSTANCE`). Carries no data; its only role is to act as the `criterionType()` key that wakes the filter up.
- `xyz.cliserkad.timber.StackDepthFilter` — `Filter<StackDepth>` that holds an inclusive `maxDepth` and samples the current stack inside `isAllowed`.

## Wiring

`Lumberjack.log` attaches `StackDepth.INSTANCE` to every `LogEvent` alongside the
`MavenLevelFilter.Level` attribute. If a `StackDepthFilter` is registered with the
global `FilterSet`, `FilterSet.isAllowed` invokes it; if not, the marker is an
inert HashMap entry — the only cost is one `HashMap.put` per log call.

## Sampling strategy

Inside `isAllowed`, the filter calls:

```java
StackWalker.getInstance().walk(s -> s.limit(maxDepth + 1).count())
```

`StackWalker` does not materialise frames it doesn't visit, and `limit(maxDepth + 1)`
short-circuits the stream as soon as the threshold is exceeded. So the per-call cost
is bounded by `O(maxDepth)`, not `O(stack.size())`. The `+ 1` lets us distinguish
"exactly `maxDepth` frames" (allow) from "deeper than `maxDepth`" (deny).

`Thread.currentThread().getStackTrace()` was rejected: it materialises the entire
stack into a `StackTraceElement[]` on every call, which is the exact cost
`StackWalker` was designed to avoid.

## Why a marker, not the depth value, as the criterion

Two alternative designs were considered:

1. **Eager sampling** — Lumberjack measures stack depth on every log call and stores it as an `Integer` (or wrapper) attribute. Rejected: pays the full sampling cost on every log call, even when no `StackDepthFilter` is registered, and can't short-circuit because the consumer might use the value differently.

2. **Lazy sampling via a marker** (chosen) — the filter measures depth itself, and gets to short-circuit using its own threshold. The marker singleton has zero allocation cost.

The trade-off: the marker pattern is slightly unusual within the existing filter
framework (where criteria are normally data, e.g. `MavenLevelFilter.Level`). It's
documented on the marker class so future readers understand why `criterion` is
ignored in `isAllowed`.

## Calibrating `maxDepth`

The sampled depth includes:

- The user code that called the logger
- All frames between user code and `Lumberjack.log`
- `Lumberjack.log` itself
- `Lumberjack.isAllowed`
- `FilterSet.isAllowed`
- `FilterSet.checkFilter`
- `StackDepthFilter.isAllowed`
- The `StackWalker.walk` lambda frame

So plan on roughly 6–8 framework frames of overhead on top of whatever user-code
depth you want to permit. The right `maxDepth` is application-specific; treat it as
a tuning knob, not a portable constant.

## Tests

`timber-core/src/test/java/xyz/cliserkad/timber/StackDepthFilterTest.java`:

- `IllegalArgumentException` when `maxDepth < 0`
- `maxDepth = 0` rejects everything (the filter's own frames already exceed 0)
- `maxDepth = Integer.MAX_VALUE` allows everything
- Recursing into a helper drives the sampled depth past a tight threshold (allow → deny crossover demonstrated)
- Integrates with `FilterSet`: a registered `StackDepthFilter(0)` causes `FilterSet.isAllowed` to deny an `AttributeMap` containing `StackDepth.INSTANCE`
- A `StackDepthFilter` does not fire on events that don't carry the `StackDepth` attribute
