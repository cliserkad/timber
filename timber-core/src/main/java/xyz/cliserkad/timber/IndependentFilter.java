package xyz.cliserkad.timber;

import org.apache.maven.plugin.logging.Log;

/**
 * A filter that evaluates based on live runtime state rather than event attributes. Unlike {@link Filter}, which receives a criterion value from the {@link AttributeMap}, an {@code IndependentFilter} decides autonomously — typically by sampling state such as the call stack.
 * <p>
 * Independent filters are always evaluated for every log event, regardless of which attributes the event carries. Register with {@link FilterSet#add(IndependentFilter)}. At most one independent filter per concrete class is active; registering a second instance of the same class replaces the first.
 */
public interface IndependentFilter extends Filter<LogEvent> {

	@Override
	boolean isAllowed(LogEvent logEvent);
	
	default boolean isAllowed(String s) {
		return isAllowed(new LogEvent(s));
	}

	@Override
	default Class<LogEvent> criterionType() {
		return LogEvent.class;
	}

}
