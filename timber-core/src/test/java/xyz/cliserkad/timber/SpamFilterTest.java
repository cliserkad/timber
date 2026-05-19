package xyz.cliserkad.timber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpamFilterTest {

	@Test
	public void rejectsNonPositiveBufferSize() {
		assertThrows(IllegalArgumentException.class, () -> new SpamFilter(0, 0.8));
		assertThrows(IllegalArgumentException.class, () -> new SpamFilter(-1, 0.8));
	}

	@Test
	public void rejectsThresholdOutOfRange() {
		assertThrows(IllegalArgumentException.class, () -> new SpamFilter(16, -0.1));
		assertThrows(IllegalArgumentException.class, () -> new SpamFilter(16, 1.1));
	}

	@Test
	public void exactDuplicateIsSuppressed() {
		SpamFilter filter = new SpamFilter();
		assertTrue(filter.isAllowed("hello world"));
		assertFalse(filter.isAllowed("hello world"));
	}

	@Test
	public void nearDuplicateIsSuppressed() {
		SpamFilter filter = new SpamFilter(16, 0.8);
		assertTrue(filter.isAllowed("Processing item 42"));
		assertFalse(filter.isAllowed("Processing item 43"));
	}

	@Test
	public void sufficientlyDifferentMessagePasses() {
		SpamFilter filter = new SpamFilter(16, 0.8);
		assertTrue(filter.isAllowed("Starting server on port 8080"));
		assertTrue(filter.isAllowed("Database connection established"));
	}

	@Test
	public void bufferWraparoundForgetsOldMessages() {
		SpamFilter filter = new SpamFilter(3, 1.0);
		assertTrue(filter.isAllowed("alpha"));
		assertTrue(filter.isAllowed("bravo"));
		assertTrue(filter.isAllowed("charlie"));
		// buffer is full; "alpha" is about to be evicted
		assertTrue(filter.isAllowed("delta"));
		// "alpha" has been evicted, so it should be allowed again
		assertTrue(filter.isAllowed("alpha"));
	}

	@Test
	public void rejectedMessageDoesNotEnterBuffer() {
		SpamFilter filter = new SpamFilter(2, 1.0);
		assertTrue(filter.isAllowed("first"));
		assertTrue(filter.isAllowed("second"));
		// "first" is still in the buffer (size 2), so exact dup is rejected
		assertFalse(filter.isAllowed("first"));
		// rejected message should NOT have displaced "second"
		assertFalse(filter.isAllowed("second"));
	}

	@Test
	public void emptyMessageHandled() {
		SpamFilter filter = new SpamFilter();
		assertTrue(filter.isAllowed(""));
		assertFalse(filter.isAllowed(""));
	}

	@Test
	public void singleCharacterMessages() {
		SpamFilter filter = new SpamFilter(16, 0.8);
		assertTrue(filter.isAllowed("a"));
		// "a" vs "b" → similarity 0.0, passes at threshold 0.8
		assertTrue(filter.isAllowed("b"));
	}

	@Test
	public void thresholdOfZeroAllowsNothing() {
		SpamFilter filter = new SpamFilter(16, 0.0);
		assertTrue(filter.isAllowed("first"));
		// any subsequent message has similarity >= 0.0 to "first"
		assertFalse(filter.isAllowed("completely different"));
	}

	@Test
	public void filterIntegratesWithFilterSet() {
		FilterSet set = new FilterSet();
		SpamFilter spam = new SpamFilter(16, 1.0);
		set.add(spam);

		assertTrue(set.isAllowed(new LogEvent("hello")));
		assertFalse(set.isAllowed(new LogEvent("hello")));
	}

	@Test
	public void filterSkippedWhenMessageContentAbsent() {
		FilterSet set = new FilterSet();
		set.add(new SpamFilter(16, 1.0));

		assertTrue(set.isAllowed(new LogEvent("")));
	}

	// --- similarity / Levenshtein unit tests ---

	@Test
	public void identicalStringsSimilarityIsOne() {
		assertEquals(1.0, SpamFilter.similarity("abc", "abc"));
	}

	@Test
	public void completelyDifferentStringsSimilarityIsLow() {
		assertTrue(SpamFilter.similarity("abc", "xyz") < 0.5);
	}

	@Test
	public void emptyStringSimilarityIsOne() {
		assertEquals(1.0, SpamFilter.similarity("", ""));
	}

	@Test
	public void oneEmptyStringSimilarityIsZero() {
		assertEquals(0.0, SpamFilter.similarity("abc", ""));
		assertEquals(0.0, SpamFilter.similarity("", "abc"));
	}

	@Test
	public void levenshteinOfIdenticalStringsIsZero() {
		assertEquals(0, SpamFilter.levenshteinDistance("test", "test"));
	}

	@Test
	public void levenshteinSingleEdit() {
		assertEquals(1, SpamFilter.levenshteinDistance("cat", "bat"));
		assertEquals(1, SpamFilter.levenshteinDistance("cat", "cats"));
		assertEquals(1, SpamFilter.levenshteinDistance("cat", "at"));
	}

	@Test
	public void levenshteinIsSymmetric() {
		assertEquals(SpamFilter.levenshteinDistance("kitten", "sitting"), SpamFilter.levenshteinDistance("sitting", "kitten"));
	}

}
