package xyz.cliserkad.timber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FilterSetTest {

	private static final Filter<String> ALLOW_ALL = new Filter<>() {

		@Override
		public boolean isAllowed(String criterion) {
			return true;
		}

		@Override
		public Class<String> criterionType() {
			return String.class;
		}

	};

	private static final Filter<String> DENY_ALL = new Filter<>() {

		@Override
		public boolean isAllowed(String criterion) {
			return false;
		}

		@Override
		public Class<String> criterionType() {
			return String.class;
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
		AttributeMap attrs = new AttributeMap();
		attrs.put("anything");
		assertTrue(set.isAllowed(attrs));
	}

	@Test
	public void filterNotCheckedWhenCriterionAbsent() {
		FilterSet set = new FilterSet();
		set.add(DENY_ALL);
		AttributeMap attrs = new AttributeMap();
		// String not present — DENY_ALL should not trigger
		assertTrue(set.isAllowed(attrs));
	}

	@Test
	public void allowedWhenFilterPasses() {
		FilterSet set = new FilterSet();
		set.add(ALLOW_ALL);
		AttributeMap attrs = new AttributeMap();
		attrs.put("value");
		assertTrue(set.isAllowed(attrs));
	}

	@Test
	public void deniedWhenFilterFails() {
		FilterSet set = new FilterSet();
		set.add(DENY_ALL);
		AttributeMap attrs = new AttributeMap();
		attrs.put("value");
		assertFalse(set.isAllowed(attrs));
	}

	@Test
	public void deniedWhenAnyFilterFails() {
		FilterSet set = new FilterSet();
		set.add(ALLOW_ALL);
		set.add(DENY_NEGATIVE);
		AttributeMap attrs = new AttributeMap();
		attrs.put("value");
		attrs.put(-1);
		assertFalse(set.isAllowed(attrs));
	}

	@Test
	public void allowedWhenAllFilterPass() {
		FilterSet set = new FilterSet();
		set.add(ALLOW_ALL);
		set.add(DENY_NEGATIVE);
		AttributeMap attrs = new AttributeMap();
		attrs.put("value");
		attrs.put(5);
		assertTrue(set.isAllowed(attrs));
	}

	@Test
	public void addReplacesPreviousFilterForSameCriterionType() {
		FilterSet set = new FilterSet();
		set.add(DENY_ALL);
		set.add(ALLOW_ALL);   // replaces DENY_ALL — same criterionType()
		AttributeMap attrs = new AttributeMap();
		attrs.put("value");
		assertTrue(set.isAllowed(attrs));
	}

}
