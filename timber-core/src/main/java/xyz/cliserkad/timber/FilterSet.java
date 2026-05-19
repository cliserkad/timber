package xyz.cliserkad.timber;

import java.util.HashMap;

/**
 * A collection of {@link Filter} and {@link IndependentFilter} instances. Criterion-based filters are keyed by their criterion type; at most one per type is active. Independent filters are keyed by their concrete class; at most one per class is active.
 * <p>
 * Criterion-based filters whose criterion type is absent from a given {@link AttributeMap} are skipped. Independent filters are always evaluated.
 */
public class FilterSet {

	private final HashMap<Class<?>, Filter<?>> filters = new HashMap<>();

	/**
	 * Registers a criterion-based {@code filter}, keyed by {@link Filter#criterionType()}. Replaces any previously registered filter for the same criterion type.
	 *
	 * @param filter the filter to register; must not be {@code null}
	 */
	public void add(Filter<?> filter) {
		filters.put(filter.criterionType(), filter);
	}

	/**
	 * Returns {@code false} if any registered filter rejects the event. Independent filters are evaluated first, then criterion-based filters whose criterion type is present in {@code attributes}.
	 *
	 * @param event the LogEvent to be tested
	 * @return {@code true} if all applicable filters pass, {@code false} otherwise
	 */
	public boolean isAllowed(LogEvent event) {
		Object attr = null;
		for(var entry : filters.entrySet()) {
			// IndependentFilters filter against all log events
			if(entry.getValue() instanceof IndependentFilter filter) {
				if(!filter.isAllowed(event))
					return false;
			} else if((attr = event.attributes.get(entry.getKey())) != null) {
				if(!checkFilter(entry.getValue(), attr))
					return false;
			}
		}
		return true;
	}

	/**
	 * Applies a single filter to a raw attribute value. {@link Filter#criterionType()}{@code .cast()} provides a runtime-checked cast; it is safe because the criterion type is the same key used to retrieve the value from the {@link AttributeMap}.
	 */
	@SuppressWarnings(
		{ "unchecked", "rawtypes" }
	)
	private static boolean checkFilter(Filter filter, Object raw) {
		return filter.isAllowed(filter.criterionType().cast(raw));
	}

}
