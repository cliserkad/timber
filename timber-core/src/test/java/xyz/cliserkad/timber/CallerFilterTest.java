package xyz.cliserkad.timber;

import com.google.common.cache.CacheLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CallerFilterTest {

	@Test
	public void rejectsEmptyBlockList() {
		assertThrows(IllegalArgumentException.class, CallerFilter::new);
	}

	@Test
	public void allowsUnblockedCaller() {
		CallerFilter filter = new CallerFilter(HelloWorldTest.class);
		assertTrue(filter.isAllowed("test"), "this test class is not blocked, so the call should be allowed");
	}

	@Test
	public void blocksMatchingCaller() {
		// Block this test class — isAllowed() should find it as the first user frame
		CallerFilter filter = new CallerFilter(CallerFilterTest.class);
		assertFalse(filter.isAllowed("test"), "this test class is blocked, so the call should be denied");
	}

	@Test
	public void blocksAnyOfMultipleClasses() {
		CallerFilter filter = new CallerFilter(HelloWorldTest.class, CallerFilterTest.class);
		assertFalse(filter.isAllowed("test"), "should be blocked when caller matches any entry in the set");
	}

	@Test
	public void filterIntegratesWithFilterSet() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter(CallerFilterTest.class));

		assertFalse(set.isAllowed(new LogEvent("test")), "FilterSet should deny when CallerFilter rejects");
	}

	@Test
	public void filterSetPassesWhenCallerNotBlocked() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter(HelloWorldTest.class));

		assertTrue(set.isAllowed(new LogEvent("test")), "FilterSet should allow when caller is not blocked");
	}

	@Test
	public void replacesExistingCallerFilter() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter(CallerFilterTest.class));
		set.add(new CallerFilter(HelloWorldTest.class));

		AttributeMap attrs = new AttributeMap();
		assertTrue(set.isAllowed(new LogEvent("test")), "second registration should replace the first");
	}

	@Test
	public void coexistsWithCriterionFilter() {
		FilterSet set = new FilterSet();
		set.add(new CallerFilter(HelloWorldTest.class));
		set.add(new SpamFilter(16, 1.0));

		assertTrue(set.isAllowed(new LogEvent("hello")), "both filters should pass independently");
	}

	@Test
	public void helperCalledFromBlockedClassStillBlocked() {
		CallerFilter filter = new CallerFilter(CallerFilterTest.class);
		assertFalse(callThroughHelper(new LogEvent("hello world"), filter), "intermediate helper should not mask the blocked caller");
	}

	private boolean callThroughHelper(LogEvent event, CallerFilter filter) {
		return filter.isAllowed(event);
	}

}
