package xyz.cliserkad.timber;

import org.apache.maven.plugin.logging.Log;
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
		assertFalse(filter.isAllowed("test"));
	}

	@Test
	public void veryLargeMaxDepthAllowsAllEvents() {
		StackDepthFilter filter = new StackDepthFilter(Integer.MAX_VALUE);
		assertTrue(filter.isAllowed("test"));
	}

	@Test
	public void deeperCallSiteFlipsAllowToDeny() {
		final int hereDepth = (int) (long) StackWalker.getInstance().walk(s -> s.count());
		// + 6 leaves headroom for the filter dispatch frames at the direct call site
		StackDepthFilter filter = new StackDepthFilter(hereDepth + 6);

		LogEvent event = new LogEvent("hello world");
		assertTrue(filter.isAllowed(event), "shallow call should pass at maxDepth=hereDepth+6");

		assertFalse(callRecursively(event, filter, 50), "recursing 50 frames should drive depth past the threshold");
	}

	private boolean callRecursively(LogEvent event, StackDepthFilter filter, int remaining) {
		if(remaining == 0) {
			return filter.isAllowed(event);
		}
		return callRecursively(event, filter, remaining - 1);
	}

	@Test
	public void filterIntegratesWithFilterSet() {
		FilterSet set = new FilterSet();
		set.add(new StackDepthFilter(0));

		assertFalse(set.isAllowed(new LogEvent()), "FilterSet should deny when StackDepthFilter rejects");
	}

	@Test
	public void replacesExistingFilterOfSameClass() {
		FilterSet set = new FilterSet();
		set.add(new StackDepthFilter(0));
		set.add(new StackDepthFilter(Integer.MAX_VALUE));

		assertTrue(set.isAllowed(new LogEvent()), "second registration should replace the first");
	}

}
