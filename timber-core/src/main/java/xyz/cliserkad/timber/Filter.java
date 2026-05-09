package xyz.cliserkad.timber;

public interface Filter<Criterion> {

	boolean isAllowed(Criterion criterion);

	Class<Criterion> criterionType();

}
