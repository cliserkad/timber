package xyz.cliserkad.timber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.cliserkad.timber.MavenLevelFilter.Level.*;

public class MavenLevelFilterTest {

	@Test
	public void criterionTypeIsLevelClass() {
		MavenLevelFilter filter = new MavenLevelFilter(INFO);
		assertEquals(MavenLevelFilter.Level.class, filter.criterionType());
	}

	@Test
	public void allowsLevelsAtOrAboveThreshold() {
		MavenLevelFilter filter = new MavenLevelFilter(WARN);
		assertFalse(filter.isAllowed(DEBUG));
		assertFalse(filter.isAllowed(INFO));
		assertTrue(filter.isAllowed(WARN));
		assertTrue(filter.isAllowed(ERROR));
	}

	@Test
	public void debugThresholdAllowsAll() {
		MavenLevelFilter filter = new MavenLevelFilter(DEBUG);
		assertTrue(filter.isAllowed(DEBUG));
		assertTrue(filter.isAllowed(INFO));
		assertTrue(filter.isAllowed(WARN));
		assertTrue(filter.isAllowed(ERROR));
	}

	@Test
	public void errorThresholdAllowsOnlyError() {
		MavenLevelFilter filter = new MavenLevelFilter(ERROR);
		assertFalse(filter.isAllowed(DEBUG));
		assertFalse(filter.isAllowed(INFO));
		assertFalse(filter.isAllowed(WARN));
		assertTrue(filter.isAllowed(ERROR));
	}

	@Test
	public void filterWorksInsideFilterSet() {
		FilterSet set = new FilterSet();
		set.add(new MavenLevelFilter(WARN));

		AttributeMap allowed = new AttributeMap();
		allowed.put(ERROR);

		AttributeMap denied = new AttributeMap();
		denied.put(DEBUG);

		assertTrue(set.isAllowed(allowed));
		assertFalse(set.isAllowed(denied));
	}

	@Test
	public void fromSfl4jLevelMapsCorrectly() {
		assertEquals(DEBUG, MavenLevelFilter.Level.fromSFL4JLevel(org.slf4j.event.Level.TRACE));
		assertEquals(DEBUG, MavenLevelFilter.Level.fromSFL4JLevel(org.slf4j.event.Level.DEBUG));
		assertEquals(INFO, MavenLevelFilter.Level.fromSFL4JLevel(org.slf4j.event.Level.INFO));
		assertEquals(WARN, MavenLevelFilter.Level.fromSFL4JLevel(org.slf4j.event.Level.WARN));
		assertEquals(ERROR, MavenLevelFilter.Level.fromSFL4JLevel(org.slf4j.event.Level.ERROR));
	}

}
