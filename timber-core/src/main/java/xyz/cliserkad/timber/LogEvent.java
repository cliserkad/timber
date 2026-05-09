package xyz.cliserkad.timber;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;

public class LogEvent {

	public final Object[] args;
	public final AttributeMap attributes;

	public LogEvent(final Object... args) {
		this.args = args;
		this.attributes = new AttributeMap();
	}

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
