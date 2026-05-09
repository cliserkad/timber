package xyz.cliserkad.timber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttributeMapTest {

	@Test
	public void putAndGet() {
		AttributeMap map = new AttributeMap();
		map.put("hello");
		assertEquals("hello", map.get(String.class));
	}

	@Test
	public void containsTrueAfterPut() {
		AttributeMap map = new AttributeMap();
		map.put(42);
		assertTrue(map.contains(Integer.class));
	}

	@Test
	public void containsFalseForAbsentType() {
		AttributeMap map = new AttributeMap();
		assertFalse(map.contains(String.class));
	}

	@Test
	public void putOverwritesPrevious() {
		AttributeMap map = new AttributeMap();
		map.put("first");
		map.put("second");
		assertEquals("second", map.get(String.class));
		assertEquals(1, map.size());
	}

	@Test
	public void getReturnsNullForAbsentType() {
		AttributeMap map = new AttributeMap();
		assertNull(map.get(String.class));
	}

	@Test
	public void isEmptyInitially() {
		assertTrue(new AttributeMap().isEmpty());
	}

	@Test
	public void sizeTracksEntries() {
		AttributeMap map = new AttributeMap();
		map.put("a");
		map.put(1);
		assertEquals(2, map.size());
	}

	@Test
	public void distinctTypesStoredIndependently() {
		AttributeMap map = new AttributeMap();
		map.put("text");
		map.put(99);
		assertEquals("text", map.get(String.class));
		assertEquals(99, map.get(Integer.class));
	}

}
