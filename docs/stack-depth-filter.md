# StackDepthFilter

A filter that suppresses log events whose call site sits deeper than a configured
maximum number of stack frames. Useful for cutting noise from helper-of-helper code
while keeping top-level orchestration messages.

## Public types

- `xyz.cliserkad.timber.StackDepthFilter` — `IndependentFilter` that holds an inclusive `maxDepth` and samples the current stack inside `isAllowed()`.

## Wiring

`StackDepthFilter` implements `IndependentFilter`, so `FilterSet` always evaluates
it for every log event when registered — no marker attribute needs to be attached
to the `LogEvent`. If no `StackDepthFilter` is registered, there is zero per-event
cost.

## Sampling strategy

Inside `isAllowed()`, the filter calls:

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

## Why IndependentFilter, not a criterion-based Filter

`StackDepthFilter` samples live runtime state (the call stack) rather than inspecting
a pre-computed attribute on the event. The `IndependentFilter` interface captures
this pattern directly — filters that need no criterion value from the `AttributeMap`.

Previously, a singleton marker class (`StackDepth.INSTANCE`) was attached to every
event just to "wake up" the filter. The `IndependentFilter` abstraction eliminates
that ceremony: independent filters are always evaluated, so no marker is needed.

## Calibrating `maxDepth`

The sampled depth includes:

- The user code that called the logger
- All frames between user code and `Lumberjack.log`
- `Lumberjack.log` itself
- `Lumberjack.isAllowed`
- `FilterSet.isAllowed`
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
- Integrates with `FilterSet`: a registered `StackDepthFilter(0)` causes `FilterSet.isAllowed` to deny
- Registering a second `StackDepthFilter` replaces the first (keyed by concrete class)
