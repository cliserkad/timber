# CallerFilter

## Purpose

`CallerFilter` suppresses log events whose direct caller belongs to a configured
set of blocked classes. It answers the question: "which class called the log
method?" and rejects the event if that class is on the blocklist.

## Design

### IndependentFilter

`CallerFilter` and `StackDepthFilter` both sample live runtime state (the call
stack) rather than inspecting event attributes. The `IndependentFilter` interface
captures this pattern — filters that need no criterion value from the
`AttributeMap`. `FilterSet` always evaluates independent filters for every event,
keyed by concrete class (so registering a second `CallerFilter` replaces the
first, matching the one-per-type behavior of criterion-based filters).

This replaced the singleton marker pattern (`StackDepth.INSTANCE`) that
previously existed just to "wake up" the `StackDepthFilter`.

### Caller resolution

`CallerFilter.isAllowed()` walks the thread stack via `StackWalker` and skips
frames belonging to the logging dispatch chain:

- `CallerFilter`, `FilterSet`, `Lumberjack` — timber internals
- `jdk.proxy*` — JDK dynamic proxy classes generated for `CombinedLogger`

The first frame outside this set is treated as the direct caller. If its
fully-qualified class name appears in the blocked set, the event is rejected.

### Thread safety

`CallerFilter` is safe to use from multiple threads. The blocked class set is
immutable (`Set.of`), and `StackWalker` is inherently thread-local.

## Usage

```java
// Suppress all logging from a noisy dependency
Lumberjack.FILTERS.add(new CallerFilter("com.example.NoisyClass"));

// Block multiple classes
Lumberjack.FILTERS.add(new CallerFilter(
    "com.example.NoisyClass",
    "com.example.VerboseHelper"
));
```

## Limitations

- Only inspects the direct caller (first user-code frame), not the full call
  chain. If `BlockedClass.foo()` calls `UnblockedClass.bar()` which logs, the
  event is allowed because the direct caller is `UnblockedClass`.
- Stack walking has a per-event cost. Unlike criterion-based filters that check
  a pre-computed attribute, `CallerFilter` must walk past the dispatch frames on
  every evaluation.
