package xyz.cliserkad.timber;

/**
 * Filters log events by minimum severity level.
 * Events at or above {@link #OUTPUT_LEVEL} are allowed; events below are suppressed.
 */
public class MavenLevelFilter implements Filter<MavenLevelFilter.Level> {

	/** The minimum severity level that this filter allows through. */
	public final Level OUTPUT_LEVEL;

	/**
	 * Returns {@code true} if {@code level}'s ordinal is at or above {@link #OUTPUT_LEVEL}.
	 */
	@Override
	public boolean isAllowed(final Level level) {
		return level.ordinal() >= OUTPUT_LEVEL.ordinal();
	}

	/**
	 * Returns {@link Level}{@code .class}, used by {@link FilterSet} to match the level
	 * attribute stored in a {@link LogEvent}'s {@link AttributeMap}.
	 */
	@Override
	public Class<Level> criterionType() {
		return Level.class;
	}

	/**
	 * Creates a filter that allows events at or above {@code level}.
	 *
	 * @param level the minimum level to allow
	 */
	public MavenLevelFilter(final Level level) {
		OUTPUT_LEVEL = level;
	}

	@Override
	public String toString() {
		return MavenLevelFilter.class.getName() + " : " + OUTPUT_LEVEL.name();
	}

	/**
	 * Severity scale used by this filter, ordered from least to most severe.
	 * Ordinal comparisons ({@code >=}) implement the threshold check.
	 */
	public enum Level {
		DEBUG,
		INFO,
		WARN,
		ERROR;

		/**
		 * Maps an SLF4J level to this enum. TRACE collapses to DEBUG because
		 * Maven's logging API does not distinguish between the two.
		 *
		 * @param level the SLF4J level to convert
		 */
		public static Level fromSFL4JLevel(final org.slf4j.event.Level level) {
			return switch(level) {
				case DEBUG, TRACE -> DEBUG;
				case INFO -> INFO;
				case WARN -> WARN;
				case ERROR -> ERROR;
			};
		}

		/**
		 * Parses a level name (case-insensitive), delegating through the SLF4J mapping.
		 *
		 * @param level a level name such as {@code "info"} or {@code "WARN"}
		 */
		public static Level fromString(final String level) {
			return fromSFL4JLevel(org.slf4j.event.Level.valueOf(level.toUpperCase()));
		}
	}

}
