package xyz.cliserkad.timber;

import java.util.HashMap;

public class FilterSet {

	private final HashMap<Class<?>, Filter<?>> filters = new HashMap<>();

	public <Criterion> void add(Filter<Criterion> filter) {
		filters.put(filter.criterionType(), filter);
	}

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean checkFilter(Filter filter, Object raw) {
		return filter.isAllowed(filter.criterionType().cast(raw));
	}

}
