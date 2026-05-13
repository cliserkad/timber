# IndependentFilter & CallerFilter — 2026-05-13

## What changed

- Added `IndependentFilter` interface for filters that evaluate live runtime state
  rather than event attributes.
- `FilterSet` now manages two collections: criterion-based (`Filter<C>`) keyed by
  criterion type, and independent (`IndependentFilter`) keyed by concrete class.
  Independent filters are always evaluated for every event.
- Rewrote `StackDepthFilter` from `Filter<StackDepth>` to `IndependentFilter`,
  removing the `isAllowed(StackDepth)` / `criterionType()` methods in favour of
  a parameterless `isAllowed()`.
- Deleted the `StackDepth` singleton marker class. It existed only to wake up
  `StackDepthFilter` via the attribute map — `IndependentFilter` eliminates that need.
- Removed `event.attributes.put(StackDepth.INSTANCE)` from `Lumberjack.log`.
- Implemented `CallerFilter` (`IndependentFilter`) that walks the call stack to find
  the first frame outside the logging dispatch chain and rejects the event if that
  class is in a configured blocked set.

## Why

The singleton marker pattern forced every filter that didn't need event data to
create a dummy marker class and attach its instance to every `LogEvent`. This was
purely ceremony — the marker carried no data, and the `criterion` parameter was
ignored in `isAllowed`. The `IndependentFilter` abstraction makes the "no criterion"
case a first-class concept and removes that boilerplate.

## Files added

- `IndependentFilter.java`
- `CallerFilter.java`
- `CallerFilterTest.java`
- `docs/caller-filter.md`
- `docs/history/2026-05-13-independent-filter.md`

## Files modified

- `FilterSet.java` — second `add` overload + independent filter evaluation loop
- `StackDepthFilter.java` — implements `IndependentFilter` instead of `Filter<StackDepth>`
- `StackDepthFilterTest.java` — updated for parameterless `isAllowed()`
- `Lumberjack.java` — removed `StackDepth.INSTANCE` attribute attachment
- `docs/stack-depth-filter.md` — updated for `IndependentFilter`
- `CLAUDE.md` — filter pipeline section rewritten

## Files deleted

- `StackDepth.java`
