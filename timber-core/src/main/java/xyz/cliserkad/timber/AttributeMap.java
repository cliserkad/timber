package xyz.cliserkad.timber;

import java.util.HashMap;

public class AttributeMap {

	private final HashMap<Class<?>, Object> map = new HashMap<>();

	public <T> void put(T value) {
		map.put(value.getClass(), value);
	}

	public <T> T get(Class<T> type) {
		return type.cast(map.get(type));
	}

	public boolean contains(Class<?> type) {
		return map.containsKey(type);
	}

	Object getRaw(Class<?> type) {
		return map.get(type);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

}
