package xyz.cliserkad.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import xyz.cliserkad.timber.Lumberjack;

/**
 * Drives {@link Lumberjack} at every SLF4J level under the {@code timber.level}
 * system property injected by surefire. The {@code System.out} {@link PrintStream}
 * (which {@code Lumberjack.log} writes to) is captured into an in-memory buffer
 * for the duration of the test, then asserted against the level-derived expectations.
 */
public class TimberLevelTest {

	@Test
	void timberLogOutputMatchesDetectedLevel() {
		final String levelProp = System.getProperty("timber.level");
		assertNotNull(levelProp, "timber.level must be set by timber:level mojo via surefire");
		final int detectedLevel = Integer.parseInt(levelProp);

		final ByteArrayOutputStream captured = new ByteArrayOutputStream();
		final PrintStream original = System.out;
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			Lumberjack.log(Level.ERROR, "error-test-message");
			Lumberjack.log(Level.WARN, "warn-test-message");
			Lumberjack.log(Level.INFO, "info-test-message");
			Lumberjack.log(Level.DEBUG, "debug-test-message");
			Lumberjack.log(Level.ERROR, "DETECTED timber.level=" + levelProp);
		} finally {
			System.setOut(original);
		}

		final String output = captured.toString(StandardCharsets.UTF_8);
		System.out.println(output);

		assertTrue(output.contains("error-test-message"),
			"ERROR must appear at every level:\n" + output);
		assertTrue(output.contains("DETECTED timber.level=" + levelProp),
			"Timber.log should echo the detected level:\n" + output);

		assertEquals(detectedLevel >= 1, output.contains("warn-test-message"),
			"WARN visibility mismatch at timber.level=" + detectedLevel + ":\n" + output);
		assertEquals(detectedLevel >= 2, output.contains("info-test-message"),
			"INFO visibility mismatch at timber.level=" + detectedLevel + ":\n" + output);
		assertEquals(detectedLevel >= 3, output.contains("debug-test-message"),
			"DEBUG visibility mismatch at timber.level=" + detectedLevel + ":\n" + output);
	}
}
