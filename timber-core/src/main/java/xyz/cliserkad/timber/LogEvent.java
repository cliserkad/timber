package xyz.cliserkad.timber;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;

/**
 * An immutable snapshot of a single log call: the raw arguments passed by the caller
 * and a typed attribute map that {@link Filter} instances inspect to decide whether
 * the event should be emitted.
 */
public class LogEvent {

	/** Raw arguments passed to the log call; {@code args[0]} is the message or format string. */
	public final Object[] args;

	/**
	 * Typed attributes attached to this event.
	 * {@link Filter} implementations read from this map by criterion type.
	 * For example, {@link MavenLevelFilter} looks up {@link MavenLevelFilter.Level}.
	 */
	public final AttributeMap attributes;

	/**
	 * Creates a log event for the given arguments with an empty attribute map.
	 *
	 * @param args the raw log arguments; {@code args[0]} should be the message or format string
	 */
	public LogEvent(final Object... args) {
		this.args = args;
		this.attributes = new AttributeMap();
	}

	/**
	 * Formats {@code args} following SLF4J {@link org.slf4j.helpers.MessageFormatter} conventions.
	 * <ul>
	 *   <li>0 args → empty string</li>
	 *   <li>1 arg  → {@code args[0].toString()}</li>
	 *   <li>2 args → SLF4J single-argument or array format depending on {@code args[1]} type</li>
	 *   <li>3 args → SLF4J two-argument or array-with-throwable format</li>
	 *   <li>4+ args → {@link Arrays#toString(Object[])}</li>
	 * </ul>
	 *
	 * @param args the arguments to format
	 * @return the formatted message string
	 */
	public static String format(Object[] args) {
		if(args.length == 0)
			return "";

		args[0] = args[0].toString();
		if(args.length == 1)
			return (String) args[0];

		final FormattingTuple tuple;
		if(args.length == 2) {
			if(args[1] instanceof Object[]) {
				tuple = MessageFormatter.arrayFormat((String) args[0], (Object[]) args[1]);
			} else {
				tuple = MessageFormatter.format((String) args[0], args[1]);
			}
		} else if(args.length == 3) {
			if(args[1] instanceof Object[] && args[2] instanceof Throwable) {
				tuple = MessageFormatter.arrayFormat((String) args[0], (Object[]) args[1], (Throwable) args[2]);
			} else {
				tuple = MessageFormatter.format((String) args[0], args[1], args[2]);
			}
		} else {
			return Arrays.toString(args);
		}
		return tuple.getMessage();
	}

	@Override
	public String toString() {
		return format(args);
	}

}
