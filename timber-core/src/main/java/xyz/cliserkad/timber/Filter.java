package xyz.cliserkad.timber;

public interface Filter<Criterion> {

	/**
	 * Determines if a given value is allowed
	 */
	boolean isAllowed(final Criterion criterion);

	/**
	 * Provides a value that is always allowed
	 */
	Criterion always();

	@SuppressWarnings("unchecked")
	default boolean isObjectAllowed(final Object object) {
		if(object == null)
			return true;
		else if(isCriterion(object))
			return isAllowed((Criterion) object);
		else
			return false;
	}

	default boolean isCriterion(Object object) {
		return isCriterion(object.getClass());
	}

	default boolean isCriterion(Class<?> clazz) {
		return clazz.equals(always().getClass());
	}

}
