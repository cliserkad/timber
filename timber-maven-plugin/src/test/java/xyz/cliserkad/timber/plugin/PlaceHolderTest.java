package xyz.cliserkad.timber.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaceHolderTest {

	@Test
	public void testPlaceHolder() {
		assertEquals(13, fib(7));
	}

	public static int fib(int n) {
		if(n <= 1)
			return n;
		else
			return fib(n - 1) + fib(n - 2);
	}

}
