package xyz.cliserkad.timber;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FilterSetTest {

	private static final Filter<Integer> ALLOW_ALL = new Filter<>() {

		@Override
		public boolean isAllowed(Integer integer) {
			return true;
		}

		@Override
		public Class<Integer> criterionType() {
			return Integer.class;
		}
	};

	private static final Filter<Integer> DENY_ALL = new Filter<>() {

		@Override
		public boolean isAllowed(Integer integer) {
			return false;
		}

		@Override
		public Class<Integer> criterionType() {
			return Integer.class;
		}
	};

	private static final Filter<Integer> DENY_NEGATIVE = new Filter<>() {

		@Override
		public boolean isAllowed(Integer criterion) {
			return criterion >= 0;
		}

		@Override
		public Class<Integer> criterionType() {
			return Integer.class;
		}

	};

	@Test
	public void emptyFilterSetAllowsEverything() {
		FilterSet set = new FilterSet();
		LogEvent event = new LogEvent();
		event.attributes.put(1);
		event.attributes.put(-1);
		assertTrue(set.isAllowed(event));
	}

	@Test
	public void filterNotCheckedWhenCriterionAbsent() {
		FilterSet set = new FilterSet();
		set.add(DENY_ALL);
		assertTrue(set.isAllowed(new LogEvent()));
	}

	@Test
	public void allowedWhenFilterPasses() {
		FilterSet set = new FilterSet();
		set.add(ALLOW_ALL);
		LogEvent event = new LogEvent();
		event.attributes.put(1);
		event.attributes.put(-1);
		assertTrue(set.isAllowed(event));
	}

	@Test
	public void deniedWhenFilterFails() {
		FilterSet set = new FilterSet();
		set.add(DENY_ALL);
		LogEvent event = new LogEvent();
		event.attributes.put(1);
		event.attributes.put(-1);
		assertFalse(set.isAllowed(event));
	}

	@Test
	public void deniedWhenAnyFilterFails() {
		FilterSet set = new FilterSet();
		set.add(ALLOW_ALL);
		set.add(DENY_NEGATIVE);
		LogEvent event = new LogEvent();
		event.attributes.put(1);
		event.attributes.put(-1);
		assertFalse(set.isAllowed(event));
	}

	@Test
	public void allowedWhenAllFilterPass() {
		FilterSet set = new FilterSet();
		set.add(ALLOW_ALL);
		set.add(DENY_NEGATIVE);
		LogEvent event = new LogEvent();
		event.attributes.put(1);
		assertTrue(set.isAllowed(event));
	}

	/**
	 @Test public void addDoesNotOverrideOnSameCriterion() {
	 FilterSet set = new FilterSet();
	 set.add(DENY_ALL);
	 set.add(ALLOW_ALL);

	 LogEvent event = new LogEvent();
	 event.attributes.put(1);
	 event.attributes.put(-1);
	 assertFalse(set.isAllowed(event));
	 }*/

}
