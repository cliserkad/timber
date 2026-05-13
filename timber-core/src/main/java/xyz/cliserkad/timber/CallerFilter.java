package xyz.cliserkad.timber;

import java.util.Set;

/**
 * Suppresses log events whose direct caller belongs to a blocked class.
 * The caller is identified by walking the current thread's stack via
 * {@link StackWalker} and selecting the first frame outside the logging
 * dispatch chain ({@link CallerFilter}, {@link FilterSet}, {@link Lumberjack},
 * and JDK dynamic proxy classes).
 * <p>
 * This filter is opt-in — it is not registered by default. Add it to the
 * {@link FilterSet} to activate class-based suppression:
 * <pre>
 *   FILTERS.add(new CallerFilter("com.example.NoisyClass"));
 * </pre>
 */
public final class CallerFilter implements IndependentFilter {

	private static final Set<String> DISPATCH_CLASSES = Set.of(
		CallerFilter.class.getName(),
		FilterSet.class.getName(),
		Lumberjack.class.getName()
	);
	private static final String JDK_PROXY_PREFIX = "jdk.proxy";

	private final Set<String> blockedClassNames;

	/**
	 * @param blockedClassNames fully-qualified names of classes whose log calls should be suppressed
	 * @throws IllegalArgumentException if no class names are provided
	 */
	public CallerFilter(final String... blockedClassNames) {
		if(blockedClassNames.length == 0) {
			throw new IllegalArgumentException("at least one class name must be specified");
		}
		this.blockedClassNames = Set.of(blockedClassNames);
	}

	/**
	 * Walks the stack to find the direct caller and returns {@code false} if
	 * that caller's class is in the blocked set.
	 */
	@Override
	public boolean isAllowed() {
		return StackWalker.getInstance().walk(frames ->
			frames.map(StackWalker.StackFrame::getClassName)
				.filter(CallerFilter::isUserFrame)
				.findFirst()
				.map(className -> !blockedClassNames.contains(className))
				.orElse(true)
		);
	}

	/** Returns {@code true} if the frame belongs to user code rather than the logging dispatch chain. */
	private static boolean isUserFrame(final String className) {
		return !DISPATCH_CLASSES.contains(className)
			&& !className.startsWith(JDK_PROXY_PREFIX);
	}

	@Override
	public String toString() {
		return CallerFilter.class.getName() + " : blocked=" + blockedClassNames;
	}

}
