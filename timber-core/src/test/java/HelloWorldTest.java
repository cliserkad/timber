package test.java;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import xyz.cliserkad.timber.Lumberjack;

public class HelloWorldTest {

	@Test
	public void testHelloWorld() {
		Lumberjack.log(Level.INFO, "Hello, World!");
		Lumberjack.log(Level.DEBUG, "Hi hi hi");
	}

}
