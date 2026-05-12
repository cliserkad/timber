package xyz.cliserkad.timber;

/**
 * Suppresses log events whose call site sits below a configured stack depth.
 * The sampled depth includes both user-code frames and the {@link Lumberjack}
 * dispatch frames; configure {@link #maxDepth} accordingly. Roughly six to
 * eight frames of dispatch overhead sit between user code and this filter, so
 * a {@code maxDepth} smaller than that will reject every event.
 * <p>The current depth is sampled inside {@link #isAllowed} via
 * {@link StackWalker#walk}, with a {@link java.util.stream.Stream#limit limit}
 * of {@code maxDepth + 1}. {@code StackWalker} does not materialise frames it
 * does not visit, so the per-call cost is bounded by {@code O(maxDepth)} rather
 * than the full stack size. The {@code + 1} lets the result distinguish
 * "exactly {@code maxDepth} frames" (allow) from "deeper" (deny).
 * <p>The {@link Filter#criterionType() criterion type} is {@link StackDepth} —
 * a singleton marker. The {@code criterion} argument to {@link #isAllowed} is
 * ignored; the depth comes from the live thread stack.
 */
public final class StackDepthFilter implements Filter<StackDepth> {

	/** Inclusive upper bound on the stack depth at which a log call may sit. */
	public final int maxDepth;

	/**
	 * @param maxDepth inclusive maximum sampled stack depth that will pass this filter
	 * @throws IllegalArgumentException if {@code maxDepth} is negative
	 */
	public StackDepthFilter(final int maxDepth) {
		if(maxDepth < 0) {
			throw new IllegalArgumentException("maxDepth must be non-negative, got " + maxDepth);
		}
		this.maxDepth = maxDepth;
	}

	/**
	 * Samples the current call stack and returns {@code true} if its depth is at
	 * or below {@link #maxDepth}. The sampling short-circuits at
	 * {@code maxDepth + 1} frames, so deep stacks do not pay the cost of being
	 * walked to the root.
	 *
	 * @param ignored the {@link StackDepth} marker is unused; depth is sampled live
	 */
	@Override
	public boolean isAllowed(final StackDepth ignored) {
		final long depth = StackWalker.getInstance().walk(s -> s.limit(maxDepth + 1L).count());
		return depth <= maxDepth;
	}

	@Override
	public Class<StackDepth> criterionType() {
		return StackDepth.class;
	}

	@Override
	public String toString() {
		return StackDepthFilter.class.getName() + " : maxDepth=" + maxDepth;
	}

}
