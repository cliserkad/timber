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
		StackDepthFilter filter = new StackDepthFilter(0);
		assertFalse(filter.isAllowed());
	}

	@Test
	public void veryLargeMaxDepthAllowsAllEvents() {
		StackDepthFilter filter = new StackDepthFilter(Integer.MAX_VALUE);
		assertTrue(filter.isAllowed());
	}

	@Test
	public void deeperCallSiteFlipsAllowToDeny() {
		final int hereDepth = (int) (long) StackWalker.getInstance().walk(s -> s.count());
		// + 6 leaves headroom for the filter dispatch frames at the direct call site
		StackDepthFilter filter = new StackDepthFilter(hereDepth + 6);

		assertTrue(filter.isAllowed(), "shallow call should pass at maxDepth=hereDepth+6");

		assertFalse(callRecursively(filter, 50), "recursing 50 frames should drive depth past the threshold");
	}

	private boolean callRecursively(StackDepthFilter filter, int remaining) {
		if(remaining == 0) {
			return filter.isAllowed();
		}
		return callRecursively(filter, remaining - 1);
	}

	@Test
	public void filterIntegratesWithFilterSet() {
		FilterSet set = new FilterSet();
		set.add(new StackDepthFilter(0));

		AttributeMap attrs = new AttributeMap();

		assertFalse(set.isAllowed(attrs), "FilterSet should deny when StackDepthFilter rejects");
	}

	@Test
	public void replacesExistingFilterOfSameClass() {
		FilterSet set = new FilterSet();
		set.add(new StackDepthFilter(0));
		set.add(new StackDepthFilter(Integer.MAX_VALUE));

		AttributeMap attrs = new AttributeMap();
		assertTrue(set.isAllowed(attrs), "second registration should replace the first");
	}

}
