# Spam Filter

## Purpose

`SpamFilter` suppresses log events whose formatted message is too similar to a recently allowed message. This prevents log spam from repeated calls with slightly varying parameters (e.g. `"Processing item 42"`, `"Processing item 43"`, ...).

## How it works

The filter maintains a fixed-size circular buffer of formatted messages that were allowed through. When a new event arrives, its formatted message is compared against every entry in the buffer using normalised Levenshtein similarity:

```
similarity = 1.0 - (editDistance / max(len1, len2))
```

If the similarity to any buffered message meets or exceeds the threshold (default 0.8), the event is rejected. Otherwise, the message is recorded into the buffer and the event passes.

Rejected messages are **not** recorded, so a repeated message only occupies one buffer slot. Once enough unique messages cycle through the buffer, the original entry is overwritten and the message can appear again.

## Criterion type

`MessageContent` wraps the `Object[] args` from the log call and lazily formats via `LogEvent.format()`. This means events rejected by earlier filters (e.g. `MavenLevelFilter`) never pay the SLF4J formatting cost. The attribute is attached in `Lumberjack.log()` alongside `MavenLevelFilter.Level` and `StackDepth.INSTANCE`.

## Configuration

| Parameter | Default | Description |
|---|---|---|
| `bufferSize` | 16 | Number of recent allowed messages to remember |
| `similarityThreshold` | 0.8 | Minimum similarity (0.0–1.0) at which a message is considered spam |

## Usage

The filter is opt-in — not registered by default:

```java
// Register with default settings
Lumberjack.addFilter(new SpamFilter());

// Or with custom settings
Lumberjack.addFilter(new SpamFilter(32, 0.9));
```

## Thread safety

The `isAllowed` method is `synchronized` because it reads and writes the shared circular buffer. Contention is expected to be low since log calls are typically fast.

## Side effects in `isAllowed`

`isAllowed` records allowed messages into the buffer, making it impure. This follows the precedent set by `StackDepthFilter`, whose `isAllowed` reads the live call stack. A minor consequence: if a later filter in the `FilterSet` rejects an event that the spam filter already allowed, the message is still recorded. In practice this is negligible because the level filter is the primary gatekeeper and rarely disagrees with subsequent filters.

## Levenshtein distance

The implementation uses the classic two-row dynamic programming algorithm, O(n×m) time and O(min(n,m)) space. For typical log messages (50–200 characters) against a buffer of 16 entries, the per-call cost is well under a millisecond.
