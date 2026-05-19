package xyz.cliserkad.timber;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

/**
 * Type-safe contract for deciding whether a value of a specific criterion type is allowed. The criterion type is
 * declared statically via {@link #criterionType()} so that {@link FilterSet} can match stored attributes without
 * reflection.
 *
 * @param <Criterion> the type of value this filter evaluates
 */
public interface Filter<Criterion> {

	/**
	 * Returns {@code true} if the given criterion passes this filter.
	 *
	 * @param criterion the value to evaluate
	 * @return {@code true} if allowed, {@code false} if the event should be suppressed
	 */
	boolean isAllowed(Criterion criterion);

	/**
	 * Returns the exact class this filter evaluates. Used as the key when registering with a {@link FilterSet}, and for
	 * the runtime cast when retrieving a matching attribute from an {@link AttributeMap}.
	 *
	 * @return the criterion class, never {@code null}
	 */
	default Class<Criterion> criterionType() {
		try {
			ParameterizedType t = (ParameterizedType) Arrays.stream(getClass().getGenericInterfaces()).filter(interfaze -> interfaze.getTypeName().contains(Filter.class.getName())).findFirst().get();
			Class<?> clazz = (Class<?>) t.getActualTypeArguments()[0];
			return (Class<Criterion>) clazz;
		} catch(Exception e) {
			throw new RuntimeException("Generic type extraction failed. criterionType() must be implemented for Filter interface.");
		}
	}

}
