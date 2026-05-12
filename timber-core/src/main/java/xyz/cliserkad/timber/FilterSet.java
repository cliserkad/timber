package xyz.cliserkad.timber;

import java.util.HashMap;

/**
 * A collection of {@link Filter} instances, each keyed by its criterion type.
 * At most one filter per criterion type is active; registering a second filter
 * for the same type replaces the first.
 * <p>
 * Filters whose criterion type is absent from a given {@link AttributeMap} are
 * skipped rather than applied, so an event is only evaluated against filters
 * for attributes it actually carries.
 */
public class FilterSet {

	private final HashMap<Class<?>, Filter<?>> filters = new HashMap<>();

	/**
	 * Registers {@code filter}, keyed by {@link Filter#criterionType()}.
	 * Replaces any previously registered filter for the same criterion type.
	 *
	 * @param filter      the filter to register; must not be {@code null}
	 * @param <Criterion> the criterion type of the filter
	 */
	public <Criterion> void add(Filter<Criterion> filter) {
		filters.put(filter.criterionType(), filter);
	}

	/**
	 * Returns {@code false} if any registered filter rejects its corresponding
	 * attribute in {@code attributes}. Filters whose criterion type is not present
	 * in the map are not evaluated.
	 *
	 * @param attributes the attribute map from a {@link LogEvent}
	 * @return {@code true} if all applicable filters pass, {@code false} otherwise
	 */
	public boolean isAllowed(AttributeMap attributes) {
		for(var entry : filters.entrySet()) {
			if(attributes.contains(entry.getKey())) {
				if(!checkFilter(entry.getValue(), attributes.getRaw(entry.getKey()))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Applies a single filter to a raw attribute value.
	 * {@link Filter#criterionType()}{@code .cast()} provides a runtime-checked cast;
	 * it is safe because the criterion type is the same key used to retrieve the
	 * value from the {@link AttributeMap}.
	 */
	@SuppressWarnings(
		{ "unchecked", "rawtypes" }
	)
	private static boolean checkFilter(Filter filter, Object raw) {
		return filter.isAllowed(filter.criterionType().cast(raw));
	}

}
