package xyz.cliserkad.timber;

import java.util.Set;

/**
 * Suppresses log events whose direct caller belongs to a blocked class. The caller is identified by walking the current
 * thread's stack via {@link StackWalker} and selecting the first frame outside the logging dispatch chain
 * ({@link CallerFilter}, {@link FilterSet}, {@link Lumberjack}, and JDK dynamic proxy classes).
 * <p>
 * This filter is opt-in — it is not registered by default. Add it to the {@link FilterSet} to activate class-based
 * suppression:
 * <pre>
 * FILTERS.add(new CallerFilter("com.example.NoisyClass"));
 * </pre>
 */
public final class CallerFilter implements IndependentFilter {

	private final Set<Class<?>> blockedClasses;

	/**
	 * @param blockedClasses classes whose log calls should be suppressed
	 * @throws IllegalArgumentException if no class names are provided
	 */
	public CallerFilter(final Class<?>... blockedClasses) {
		if(blockedClasses.length == 0) {
			throw new IllegalArgumentException("at least one class name must be specified");
		}
		this.blockedClasses = Set.of(blockedClasses);
	}

	/**
	 * Walks the stack to see if any blocked classes are involved in the method call chain
	 */
	@Override
	public boolean isAllowed(LogEvent ignored) {
		return !StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.anyMatch(frame -> blockedClasses.contains(frame.getDeclaringClass())));
	}

	@Override
	public String toString() {
		return CallerFilter.class.getName() + " : blocked=" + blockedClasses;
	}

}
