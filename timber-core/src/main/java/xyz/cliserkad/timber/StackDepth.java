package xyz.cliserkad.timber;

/**
 * Singleton marker attached to every {@link LogEvent} by {@link Lumberjack#log}.
 * Its sole purpose is to act as the {@link Filter#criterionType()} key that
 * activates a {@link StackDepthFilter} (if one is registered with the
 * {@link FilterSet}).
 *
 * <p>The marker carries no data — the actual stack depth is sampled lazily inside
 * {@link StackDepthFilter#isAllowed} so that:
 * <ul>
 *   <li>events without a registered {@code StackDepthFilter} pay nothing beyond a
 *       single {@link java.util.HashMap#put} on the attribute map, and</li>
 *   <li>filtered events can short-circuit the {@link StackWalker} traversal at the
 *       configured threshold rather than walking the full stack.</li>
 * </ul>
 *
 * <p>The instance is safe to share across threads; it has no mutable state.
 */
public final class StackDepth {

	/** The single instance attached to every {@link LogEvent}. */
	public static final StackDepth INSTANCE = new StackDepth();

	private StackDepth() {
	}

	@Override
	public String toString() {
		return StackDepth.class.getSimpleName();
	}
}
