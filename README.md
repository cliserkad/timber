# timber

A logging filter framework for Maven builds. Timber bridges SLF4J and Maven plugin logging through a single dynamic proxy and provides pluggable filters to suppress noise at runtime.

## Modules

- **timber-core** — the logging library
- **timber-maven-plugin** — detects the Maven log level

## Build & test

Requires Java 21 and Maven 3.9.8+.

Build and execute unit / integration tests

```bash
./scripts/build.sh
```

Format Java code using Spotless:apply

```bash
./scripts/format.sh
```

## Architecture

### Lumberjack

`Lumberjack` is the central dispatcher. It implements `InvocationHandler` and `ILoggerFactory`, creating a single dynamic-proxy instance that satisfies both SLF4J's `Logger` and Maven's `Log` via the `CombinedLogger` interface. Output goes to `System.out` with Maven-style color-coded level prefixes. When DEBUG is enabled, call-site info (file, line, thread) is appended.

### Filter pipeline

Every log call passes through a `FilterSet`. One rejection from any filter suppresses the event.

There are two kinds of filter:

- **Criterion-based** (`Filter<C>`) — keyed by criterion type, only evaluated when that type is present in the event's `AttributeMap`.
- **Independent** (`IndependentFilter`) — evaluated for every event, typically sampling runtime state like the call stack.

### Level detection

The `timber:level` mojo probes `getLog().is{Debug,Info,Warn,Error}Enabled()` and writes the result to `timber.level` in project, session, and system properties. `Lumberjack` reads this at startup to configure its `MavenLevelFilter`.

## Public API

### Logging

```java
Lumberjack.log("hello world");                         // INFO by default
Lumberjack.

log(Level.WARN, "disk usage at {}%",92);    // SLF4J-style formatting
Lumberjack.

log(Level.ERROR, "fatal:",throwable);
```

### Built-in filters

**MavenLevelFilter** — minimum severity threshold. Registered automatically at startup from `timber.level`. Uses a four-value `Level` enum (DEBUG, INFO, WARN, ERROR) that collapses SLF4J's TRACE into DEBUG.

```java
Lumberjack.FILTERS.add(new MavenLevelFilter(MavenLevelFilter.Level.WARN));
```

**SpamFilter** — suppresses near-duplicate messages using normalized Levenshtein distance. Maintains a circular buffer of recent messages (default size 16, similarity threshold 0.8).

```java
Lumberjack.FILTERS.add(new SpamFilter(16, 0.8));
```

**StackDepthFilter** — rejects log calls whose call stack exceeds a maximum depth. Uses `StackWalker` with a bounded `limit()` so cost is O(maxDepth), not O(full stack).

```java
Lumberjack.FILTERS.add(new StackDepthFilter(30));
```

**CallerFilter** — suppresses log calls originating from specific classes.

```java
Lumberjack.FILTERS.add(new CallerFilter(NoisyHelper.class, VerboseLib .class));
```

### `timber:level` mojo

Bind to an early phase so `timber.level` is available to downstream plugins:

```xml

<plugin>
	<groupId>xyz.cliserkad</groupId>
	<artifactId>timber-maven-plugin</artifactId>
	<version>0.0.1</version>
	<executions>
		<execution>
			<id>detect-level</id>
			<phase>validate</phase>
			<goals>
				<goal>level</goal>
			</goals>
		</execution>
	</executions>
</plugin>

<plugin>
<artifactId>maven-surefire-plugin</artifactId>
<configuration>
	<systemPropertyVariables>
		<timber.level>${timber.level}</timber.level>
	</systemPropertyVariables>
</configuration>
</plugin>
```
