package xyz.cliserkad.timber;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class TypeMap<T> {

	private final HashMap<Class<?>, T> map;

	public TypeMap() {
		map = new HashMap<>();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Class<?> key) {
		return map.containsKey(key);
	}

	public boolean containsValue(T value) {
		return map.containsValue(value);
	}

	public T get(Class<?> key) {
		return map.get(key);
	}

	public T put(T value) {
		return map.put(value.getClass(), value);
	}

	public Object remove(Class<?> key) {
		return map.remove(key);
	}

	public void clear() {
		map.clear();
	}

	public Set<Class<?>> keySet() {
		return map.keySet();
	}

	public Collection<T> values() {
		return map.values();
	}

	public Set<Entry<Class<?>, T>> entrySet() {
		return map.entrySet();
	}

}
