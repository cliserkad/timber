package xyz.cliserkad.timber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CallerFilterTest {

	@Test
	public void rejectsEmptyBlockList() {
		assertThrows(IllegalArgumentException.class, CallerFilter::new);
	}

	@Test
	public void allowsUnblockedCaller() {
		CallerFilter filter = new CallerFilter("com.example.NoisyClass");
		assertTrue(filter.isAllowed(), "this test class is not blocked, so the call should be allowed");
	}

	@Test
	public void blocksMatchingCaller() {
		// Block this test class — isAllowed() should find it as the first user frame
		CallerFilter filter = new CallerFilter(CallerFilterTest.class.getName());
		assertFalse(filter.isAllowed(), "this test class is blocked, so the call should be denied");
	}

	@Test
	public void blocksAnyOfMultipleClasses() {
		CallerFilter filter = new CallerFilter("com.example.Other", CallerFilterTest.class.getName());
		assertFalse(filter.isAllowed(), "should be blocked when caller matches any entry in the set");
	}

	@Test
	public void filterIntegratesWithFilterSet() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter(CallerFilterTest.class.getName()));

		AttributeMap attrs = new AttributeMap();
		assertFalse(set.isAllowed(attrs), "FilterSet should deny when CallerFilter rejects");
	}

	@Test
	public void filterSetPassesWhenCallerNotBlocked() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter("com.example.NoisyClass"));

		AttributeMap attrs = new AttributeMap();
		assertTrue(set.isAllowed(attrs), "FilterSet should allow when caller is not blocked");
	}

	@Test
	public void replacesExistingCallerFilter() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter(CallerFilterTest.class.getName()));
		set.add(new CallerFilter("com.example.NoisyClass"));

		AttributeMap attrs = new AttributeMap();
		assertTrue(set.isAllowed(attrs), "second registration should replace the first");
	}

	@Test
	public void coexistsWithCriterionFilter() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter("com.example.NoisyClass"));
		set.add(new SpamFilter(16, 1.0));

		AttributeMap attrs = new AttributeMap();
		attrs.put("hello");
		assertTrue(set.isAllowed(attrs), "both filters should pass independently");
	}

	@Test
	public void helperCalledFromBlockedClassStillBlocked() {
		CallerFilter filter = new CallerFilter(CallerFilterTest.class.getName());
		assertFalse(callThroughHelper(filter), "intermediate helper should not mask the blocked caller");
	}

	private boolean callThroughHelper(CallerFilter filter) {
		return filter.isAllowed();
	}

}
