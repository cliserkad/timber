package xyz.cliserkad.timber;

/**
 * A filter that evaluates based on live runtime state rather than event attributes.
 * Unlike {@link Filter}, which receives a criterion value from the {@link AttributeMap},
 * an {@code IndependentFilter} decides autonomously — typically by sampling state
 * such as the call stack.
 * <p>
 * Independent filters are always evaluated for every log event, regardless of which
 * attributes the event carries. Register with {@link FilterSet#add(IndependentFilter)}.
 * At most one independent filter per concrete class is active; registering a second
 * instance of the same class replaces the first.
 */
public interface IndependentFilter {

	/**
	 * Returns {@code true} if the current log event should be emitted.
	 *
	 * @return {@code true} if allowed, {@code false} if the event should be suppressed
	 */
	boolean isAllowed();

}
