package xyz.cliserkad.timber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackDepthFilterTest {

	@Test
	public void rejectsNegativeMaxDepth() {
		assertThrows(IllegalArgumentException.class, () -> new StackDepthFilter(-1));
	}

	@Test
	public void zeroMaxDepthRejectsAllEvents() {
		// The filter's own frames already exceed 0, so no live call site can pass
		StackDepthFilter filter = new StackDepthFilter(0);
		assertFalse(filter.isAllowed(StackDepth.INSTANCE));
	}

	@Test
	public void veryLargeMaxDepthAllowsAllEvents() {
		StackDepthFilter filter = new StackDepthFilter(Integer.MAX_VALUE);
		assertTrue(filter.isAllowed(StackDepth.INSTANCE));
	}

	@Test
	public void criterionTypeIsStackDepthClass() {
		assertEquals(StackDepth.class, new StackDepthFilter(10).criterionType());
	}

	@Test
	public void deeperCallSiteFlipsAllowToDeny() {
		// Pick a maxDepth that lets the direct call pass but a recursion deeper than
		// the threshold tip over.  We sample once to anchor against the JUnit stack.
		final int hereDepth = (int) (long) StackWalker.getInstance().walk(s -> s.count());
		// + 6 leaves headroom for the filter dispatch frames at the direct call site
		StackDepthFilter filter = new StackDepthFilter(hereDepth + 6);

		assertTrue(filter.isAllowed(StackDepth.INSTANCE), "shallow call should pass at maxDepth=hereDepth+6");

		assertFalse(callRecursively(filter, 50), "recursing 50 frames should drive depth past the threshold");
	}

	private boolean callRecursively(StackDepthFilter filter, int remaining) {
		if(remaining == 0) {
			return filter.isAllowed(StackDepth.INSTANCE);
		}
		return callRecursively(filter, remaining - 1);
	}

	@Test
	public void filterIntegratesWithFilterSet() {
		FilterSet set = new FilterSet();
		set.add(new StackDepthFilter(0));

		AttributeMap attrs = new AttributeMap();
		attrs.put(StackDepth.INSTANCE);

		assertFalse(set.isAllowed(attrs), "FilterSet should deny when StackDepthFilter rejects");
	}

	@Test
	public void filterSkippedWhenStackDepthAttributeAbsent() {
		FilterSet set = new FilterSet();
		set.add(new StackDepthFilter(0));

		AttributeMap attrs = new AttributeMap();
		// no StackDepth attribute → filter should not run
		assertTrue(set.isAllowed(attrs));
	}

}
