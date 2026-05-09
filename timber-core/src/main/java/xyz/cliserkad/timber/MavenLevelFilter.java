package xyz.cliserkad.timber;

public class MavenLevelFilter implements Filter<MavenLevelFilter.Level> {

	public final Level OUTPUT_LEVEL;

	@Override
	public boolean isAllowed(final Level level) {
		return level.ordinal() >= OUTPUT_LEVEL.ordinal();
	}

	@Override
	public Class<Level> criterionType() {
		return Level.class;
	}

	public MavenLevelFilter(final Level level) {
		OUTPUT_LEVEL = level;
	}

	@Override
	public String toString() {
		return MavenLevelFilter.class.getName() + " : " + OUTPUT_LEVEL.name();
	}

	public enum Level {
		DEBUG,
		INFO,
		WARN,
		ERROR;

		public static Level fromSFL4JLevel(final org.slf4j.event.Level level) {
			return switch(level) {
				case DEBUG, TRACE -> DEBUG;
				case INFO -> INFO;
				case WARN -> WARN;
				case ERROR -> ERROR;
			};
		}

		public static Level fromString(final String level) {
			return fromSFL4JLevel(org.slf4j.event.Level.valueOf(level.toUpperCase()));
		}
	}

}
