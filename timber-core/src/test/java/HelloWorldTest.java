package test.java;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import xyz.cliserkad.timber.core.Lumberjack;

public class HelloWorldTest {

	@Test
	public void testHelloWorld() {
		Lumberjack.log(Level.INFO, "Hello, World!");
		Lumberjack.log(Level.DEBUG, "Hi hi hi");
		Lumberjack.log(Level.ERROR, "some error");
		Lumberjack.log(Level.WARN, "some warning");
	}

}
