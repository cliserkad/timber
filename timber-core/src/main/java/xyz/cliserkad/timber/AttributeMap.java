package xyz.cliserkad.timber;

import java.util.HashMap;

/**
 * A type-keyed map that stores at most one value per concrete runtime type.
 * Values are keyed by {@code value.getClass()}, so a subclass instance is
 * stored under its actual runtime type, not any declared supertype.
 */
public class AttributeMap {

	private final HashMap<Class<?>, Object> map = new HashMap<>();

	/**
	 * Stores {@code value} under its runtime class, replacing any previous value of the same type.
	 *
	 * @param value the value to store; must not be {@code null}
	 * @param <T> inferred type of the value
	 */
	public <T> void put(T value) {
		map.put(value.getClass(), value);
	}

	/**
	 * Returns the value stored under {@code type}, or {@code null} if absent.
	 * Uses {@link Class#cast} for a checked retrieval.
	 *
	 * @param type the key to look up
	 * @param <T> the expected value type
	 * @return the stored value, or {@code null}
	 */
	public <T> T get(Class<T> type) {
		return type.cast(map.get(type));
	}

	/**
	 * Returns {@code true} if a value has been stored under {@code type}.
	 *
	 * @param type the key to check
	 */
	public boolean contains(Class<?> type) {
		return map.containsKey(type);
	}

	/**
	 * Package-private raw access used by {@link FilterSet} to retrieve a value by a
	 * criterion type token without needing a matching generic type parameter at the call site.
	 */
	Object getRaw(Class<?> type) {
		return map.get(type);
	}

	/** Returns the number of distinct types stored in this map. */
	public int size() {
		return map.size();
	}

	/** Returns {@code true} if no values have been stored. */
	public boolean isEmpty() {
		return map.isEmpty();
	}

}
