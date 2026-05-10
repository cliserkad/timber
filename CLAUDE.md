# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & test commands

Multi-module Maven build, Java 21, version is `${revision}` (currently `0.0.1`) resolved by the `flatten-maven-plugin`.

```bash
# Full build, including IT projects under timber-maven-plugin/src/it
mvn verify

# Install everything to local repo, skipping unit tests AND invoker ITs
mvn -DskipTests -Dinvoker.skip=true install

# Re-run only the maven-plugin's IT suite (timber-core must already be installed)
mvn verify -pl timber-maven-plugin

# Run a single unit test in timber-core
mvn -pl timber-core test -Dtest=FilterSetTest
mvn -pl timber-core test -Dtest=FilterSetTest#methodName

# Skip the invoker ITs while iterating
mvn verify -Dinvoker.skip=true
```

The invoker plugin is bound to `verify` and runs three IT projects in `timber-maven-plugin/src/it/{info,debug,warn}-level/`. Its `install` goal installs `timber-maven-plugin` into the local repo before invoking those projects, but `timber-core` must be installed beforehand — which `mvn verify` from the root does naturally because of reactor ordering.

## Architecture

Two modules:

- **timber-core** — the logging library
- **timber-maven-plugin** — a Mojo (`timber:level`) that detects Maven's active log level and exports it as the `timber.level` project/session property

### Lumberjack as a dynamic proxy

`Lumberjack` is the entry point. It implements both `InvocationHandler` and `org.slf4j.ILoggerFactory`. A static `PROXY` is created via `Proxy.newProxyInstance` against `CombinedLogger`, an interface that extends both `org.slf4j.Logger` and `org.apache.maven.plugin.logging.Log`. So a single proxy instance satisfies both SLF4J consumers and Maven plugin consumers without branching.

The `invoke` handler routes by method name:

- `is(Trace|Debug|Info|Warn|Error)Enabled` → `isLevelEnabled(Level)`
- `trace|debug|info|warn|error` → `log(Level, Object[])`
- everything else → `forwardImplemented` (reflective lookup of a matching method on `Lumberjack` itself, e.g. `getLogger`)

`Lumberjack.log` writes directly to `System.out.println` after applying filters; it does NOT delegate to an SLF4J binding. When DEBUG is enabled, the call site (file + line) is appended via stack-trace inspection at index 3 — adding wrapper static methods would shift this index.

### Filter pipeline (post-redesign — see filter-redesign.md)

The filter system was redesigned to remove a reflection-heavy `TypeMap`/`Filter.always()` pattern. The current shape:

- `Filter<C>` declares its criterion type via `criterionType()` (no instance probe).
- `AttributeMap` is a `Class<?> → Object` map keyed by the value's runtime class.
- `FilterSet` stores filters keyed by `criterionType()`. `isAllowed(AttributeMap)` only evaluates filters whose criterion type is actually present on the event. The single unchecked cast is in `FilterSet.checkFilter`, justified because the key used to look up the value IS the cast target.
- `LogEvent` carries `args` plus an `AttributeMap`; callers populate attributes (e.g. `event.attributes.put(MavenLevelFilter.Level.fromSFL4JLevel(level))`) and `Lumberjack.isAllowed(event)` delegates to the `FilterSet`.

`MavenLevelFilter` is the only built-in `Filter`. It uses its own four-value `Level` enum (DEBUG/INFO/WARN/ERROR) — distinct from `org.slf4j.event.Level` because Maven's logging API doesn't distinguish TRACE from DEBUG. `Level.fromSFL4JLevel` collapses the two.

### Level detection

`LogLevelDetector` (the `timber:level` mojo) probes `getLog().is{Error,Warn,Info,Debug}Enabled()` starting from `-1` and increments per enabled level, yielding 0 (errors only / `-q`), 2 (default INFO), or 3 (`-X` debug). The result is written to `project.properties`, `session.userProperties`, AND `session.systemProperties` under `timber.level`. `Lumberjack`'s static initializer reads `System.getProperty("timber.level")` and indexes `org.slf4j.event.Level.values()` (which
is `[ERROR, WARN, INFO, DEBUG, TRACE]`, ordinals 0–4) to set `OUTPUT_LEVEL`. `isLevelEnabled(level)` is `level.ordinal() <= OUTPUT_LEVEL.ordinal()`.

### IT testing pattern

`timber-maven-plugin/src/it/{info,debug,warn}-level/` each contain a standalone Maven project that:

1. Runs `timber:level` at `validate` phase to set `timber.level` in project properties.
2. Surefire forwards `${timber.level}` into the test JVM via `<systemPropertyVariables>` (lazy evaluation — by the time surefire executes, the mojo has already set the property).
3. A shared `TimberLevelTest` JUnit class captures `System.out` into a `ByteArrayOutputStream`, calls `Lumberjack.log` at all four levels (and once more at ERROR to print `DETECTED timber.level=N`), then asserts which messages appear in the captured buffer based on the detected level. The `warn-level` IT additionally passes `MAVEN_ARGS=-q`; the `debug-level` IT sets `invoker.debug=true`.
4. `verify.bsh` (BeanShell, NOT Groovy — Groovy 4's bundled ASM doesn't support Java 21+ class files) just checks the surefire XML report exists. Real verification is the in-test JUnit assertions.

Each IT project must declare `slf4j-api`, `maven-plugin-api`, and `maven-shared-utils` explicitly because timber-core inherits those as `provided` from the parent and so does not export them transitively.

## Conventions

- Tabs for indentation in Java sources.
- Javadoc on public types/methods. Comments lead with the *why* (especially for the proxy/cast tricks).
- `@SuppressWarnings({"unchecked","rawtypes"})` is acceptable when the cast is structurally guaranteed by the surrounding contract (see `FilterSet.checkFilter`).

## Documentation Instructions

- Always document architectural and implementation analysis, insights, and plans as .md files in /docs
- Regularly update existing files, especially CLAUDE.md, README.md and /docs/*
- Preserve the history of the project by continuously moving descriptions of modifications to /docs/history
