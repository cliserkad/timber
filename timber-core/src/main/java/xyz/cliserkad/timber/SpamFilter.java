package xyz.cliserkad.timber;

/**
 * Suppresses log events whose formatted message is too similar to a recently allowed message. Similarity is measured by normalised Levenshtein distance: {@code 1.0 - (editDistance / max(len1, len2))}. If the similarity to any message in the circular buffer meets or exceeds the configured threshold, the event is rejected.
 * <p>Allowed messages are recorded into the buffer; rejected messages are not,
 * so a repeated message only ever occupies one slot. Once the buffer is full the oldest entry is overwritten, allowing the same message to pass again after enough unique messages have intervened.
 * <p>The buffer and head pointer are guarded by {@code synchronized} so the
 * filter is safe to use from multiple threads.
 */
public final class SpamFilter implements IndependentFilter {

	public static final int DEFAULT_BUFFER_SIZE = 16;
	public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.8;

	private final String[] buffer;
	private final double similarityThreshold;
	private int head = 0;
	private int count = 0;

	public SpamFilter() {
		this(DEFAULT_BUFFER_SIZE, DEFAULT_SIMILARITY_THRESHOLD);
	}

	/**
	 * @param bufferSize          number of recent messages to remember
	 * @param similarityThreshold minimum similarity (0.0–1.0) at which a message is considered spam
	 * @throws IllegalArgumentException if bufferSize is not positive or threshold is outside [0,1]
	 */
	public SpamFilter(final int bufferSize, final double similarityThreshold) {
		if(bufferSize <= 0) {
			throw new IllegalArgumentException("bufferSize must be positive, got " + bufferSize);
		}
		if(similarityThreshold < 0.0 || similarityThreshold > 1.0) {
			throw new IllegalArgumentException("similarityThreshold must be in [0,1], got " + similarityThreshold);
		}
		this.buffer = new String[bufferSize];
		this.similarityThreshold = similarityThreshold;
	}

	/**
	 * Returns {@code true} if the formatted message is sufficiently unique compared to every message in the buffer. When allowed, the message is recorded into the buffer.
	 */
	@Override
	public boolean isAllowed(LogEvent logEvent) {
		final String message = logEvent.toString();
		for(int i = 0; i < count; i++) {
			int index = (head - 1 - i + buffer.length) % buffer.length;
			if(similarity(message, buffer[index]) >= similarityThreshold) {
				return false;
			}
		}
		buffer[head] = message;
		head = (head + 1) % buffer.length;
		if(count < buffer.length) {
			count++;
		}
		return true;
	}

	/**
	 * Normalised similarity between two strings: {@code 1.0 - (editDistance / maxLen)}. Returns 1.0 for equal strings and 0.0 when every character differs.
	 */
	static double similarity(final String a, final String b) {
		final int maxLen = Math.max(a.length(), b.length());
		if(maxLen == 0 || a.equals(b)) {
			return 1.0;
		}
		return 1.0 - ((double) levenshteinDistance(a, b) / maxLen);
	}

	/**
	 * Classic two-row Levenshtein distance. The shorter string is always used as the "column" dimension so the two working arrays are as small as possible.
	 */
	static int levenshteinDistance(String a, String b) {
		if(a.length() > b.length()) {
			final String tmp = a;
			a = b;
			b = tmp;
		}
		int[] prev = new int[a.length() + 1];
		int[] curr = new int[a.length() + 1];
		for(int i = 0; i <= a.length(); i++) {
			prev[i] = i;
		}
		for(int j = 1; j <= b.length(); j++) {
			curr[0] = j;
			for(int i = 1; i <= a.length(); i++) {
				final int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				curr[i] = Math.min(Math.min(curr[i - 1] + 1, prev[i] + 1), prev[i - 1] + cost);
			}
			final int[] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		return prev[a.length()];
	}

	@Override
	public String toString() {
		return SpamFilter.class.getName() + " : bufferSize=" + buffer.length + ", threshold=" + similarityThreshold;
	}

}
