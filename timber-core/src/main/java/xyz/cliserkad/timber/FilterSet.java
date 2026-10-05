package xyz.cliserkad.timber;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A collection of {@link Filter} and {@link IndependentFilter} instances. Criterion-based filters are keyed by their
 * criterion type.
 * <p>
 * Criterion-based filters whose criterion type is absent from a given {@link AttributeMap} are skipped. Independent
 * filters are always evaluated.
 */
public class FilterSet {

	private final Map<Class<?>, Set<Filter<?>>> filters = new HashMap<>();

	/**
	 * Registers a criterion-based {@code filter}, keyed by {@link Filter#criterionType()}.
	 *
	 * @param filter the filter to register; must not be {@code null}
	 */
	public void add(Filter<?> filter) {
		Set<Filter<?>> filterSet = filters.get(filter.criterionType());
		if(filterSet == null) {
			filterSet = new HashSet<>();
			filters.put(filter.criterionType(), filterSet);
		}
		filterSet.add(filter);
	}

	public void remove(Filter<?> filter) {
		final Set<Filter<?>> filterSet = filters.get(filter.criterionType());
		if(filterSet == null)
			return;
		filterSet.remove(filter);
	}

	/**
	 * Returns {@code false} if any registered filter rejects the event. Independent filters are evaluated first, then
	 * criterion-based filters whose criterion type is present in {@code attributes}.
	 *
	 * @param event the LogEvent to be tested
	 * @return {@code true} if all applicable filters pass, {@code false} otherwise
	 */
	public boolean isAllowed(LogEvent event) {
		Set<Filter<?>> matchingFilters;

		// test IndependentFilters first
		if((matchingFilters = filters.get(LogEvent.class)) != null)
			for(Filter<?> filter : filters.get(LogEvent.class))
				if(!checkFilter(filter, event))
					return false;

		for(Class<?> attributeType : event.attributes.types())
			if((matchingFilters = filters.get(attributeType)) != null)
				for(Filter<?> filter : matchingFilters)
					if(!checkFilter(filter, event.attributes.get(attributeType)))
						return false;

		return true;
	}

	/**
	 * Applies a single filter to a raw attribute value. {@link Filter#criterionType()}{@code .cast()} provides a
	 * runtime-checked cast; it is safe because the criterion type is the same key used to retrieve the value from the
	 * {@link AttributeMap}.
	 */
	@SuppressWarnings(
		{ "unchecked", "rawtypes" }
	)
	private static boolean checkFilter(Filter filter, Object raw) {
		return filter.isAllowed(filter.criterionType().cast(raw));
	}

}
