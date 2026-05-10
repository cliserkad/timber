# timber

A logging library that bridges SLF4J and Maven plugin logging through a single proxy, plus a Maven plugin that detects the active build log level and exposes it to downstream consumers.

## Modules

- **timber-core** — the logging library (`Lumberjack`, `Filter`, `AttributeMap`, `FilterSet`).
- **timber-maven-plugin** — provides the `timber:level` goal.

## Build & test

Multi-module Maven build, Java 21, version `${revision}` (currently `0.0.1`) resolved by the `flatten-maven-plugin`.

```bash
# Full build, including the IT projects under timber-maven-plugin/src/it
mvn clean verify

# Skip the invoker ITs while iterating
mvn verify -Dinvoker.skip=true
```

## Architecture

### Lumberjack

`Lumberjack` is the entry point. A single dynamic-proxy instance satisfies both `org.slf4j.Logger` and `org.apache.maven.plugin.logging.Log` consumers, exposed through the `CombinedLogger` interface. Output is written directly to `System.out`. When DEBUG is enabled, the call site (file + line) is appended to each line.

### Filter pipeline

- `Filter<C>` declares its criterion type via `criterionType()`.
- `AttributeMap` is a `Class<?> → Object` map keyed by the value's runtime class.
- `FilterSet` stores filters keyed by `criterionType()` and only evaluates filters whose criterion type is actually present on the event.
- `LogEvent` carries the raw arguments plus an `AttributeMap` of typed attributes.

`MavenLevelFilter` is the only built-in `Filter`. It uses its own four-value `Level` enum (DEBUG/INFO/WARN/ERROR) — distinct from `org.slf4j.event.Level` because Maven's logging API doesn't distinguish TRACE from DEBUG. `MavenLevelFilter.Level.fromSFL4JLevel` collapses the two.

### Level detection

The `timber:level` mojo probes `getLog().is{Error,Warn,Info,Debug}Enabled()` starting from `-1` and increments per enabled level, yielding 0 (errors only / `-q`), 2 (default INFO), or 3 (`-X` debug). The result is written to `project.properties`, `session.userProperties`, and `session.systemProperties` under `timber.level`. `Lumberjack` reads `System.getProperty("timber.level")` at startup to set its output threshold.

## Public API

### Logging — `xyz.cliserkad.timber.Lumberjack`

```java
Lumberjack.log("hello world");                        // INFO by default
Lumberjack.

log(Level.WARN, "disk usage at {}%",92);  // SLF4J-style formatting
Lumberjack.

log(Level.ERROR, "fatal:",throwable);
```

### Custom filters

Implement `Filter<C>` and register with the global `FilterSet` (or your own):

```java
public class TenantFilter implements Filter<TenantId> {

	private final Set<TenantId> allowed;

	@Override
	public boolean isAllowed(TenantId t) {
		return allowed.contains(t);
	}

	@Override
	public Class<TenantId> criterionType() {
		return TenantId.class;
	}

}
```

### `timber:level` mojo

Bind to an early phase to make `timber.level` available to later plugins (e.g. surefire's `systemPropertyVariables`):

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

The mojo also logs `Output level: N` at INFO so the detected value appears in the build log.
