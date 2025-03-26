package xyz.cliserkad.timber.core;

import org.apache.maven.shared.utils.logging.MessageUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Attempts to route logging to Maven Plugin / Mojo output during Maven builds. Falls back to System.out if routing fails.
 */
public class Lumberjack implements InvocationHandler, ILoggerFactory {

	private static final Lumberjack INSTANCE = new Lumberjack();
	private static final CombinedLogger PROXY = (CombinedLogger) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { CombinedLogger.class }, INSTANCE);
	private static final Level OUTPUT_LEVEL;

	static {
		MessageUtils.setColorEnabled(true);
		// FIXME: getting level from system properties isn't the best idea
		OUTPUT_LEVEL = System.getProperties().getProperty("timber.level") != null ? Level.values()[Integer.parseInt(System.getProperties().getProperty("timber.level"))] : Level.INFO;
	}

	private Lumberjack() {
		// Prevent instantiation
	}

	public static void log(Object... args) {
		log(null, args);
	}

	public static void log(Level level, Object... args) {
		if(level == null)
			level = Level.INFO;
		if(level.ordinal() > OUTPUT_LEVEL.ordinal()) {
			return;
		}
		final String prepend;
		switch(level) {
			case TRACE:
			case DEBUG: {
				prepend = MessageUtils.level().debug(level.name());
				break;
			}
			case WARN: {
				prepend = MessageUtils.level().warning(level.name());
				break;
			}
			case ERROR: {
				prepend = MessageUtils.level().error(level.name());
				break;
			}
			case INFO:
			default: {
				prepend = MessageUtils.level().info(level.name());
				break;
			}
		}
		// add callsite info if debug is enabled
		if(isLevelEnabled(Level.DEBUG))
			System.out.println("[" + prepend + "] " + format(args) + " " + logCallsiteInfo());
		else
			System.out.println("[" + prepend + "] " + format(args));
	}

	private static String logCallsiteInfo() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		final StackTraceElement caller = stackTrace[3];
		if(!Thread.currentThread().getName().equals("main"))
			return "(" + caller.getFileName() + ":" + caller.getLineNumber() + ") {" + Thread.currentThread().getName() + "}";
		else
			return "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")";
	}

	public static CombinedLogger combinedLogger() {
		return PROXY;
	}

	public static String getName() {
		return Lumberjack.class.getName();
	}

	private static Method matchMethod(Method requested) {
		for(Method implemented : INSTANCE.getClass().getDeclaredMethods()) {
			if(methodsMatch(requested, implemented)) {
				return implemented;
			}
		}
		return null;
	}

	public static boolean methodsMatch(Method requested, Method actual) {
		if(requested.getName().equals(actual.getName())) {
			if(!requested.getReturnType().equals(actual.getReturnType()))
				return false;
			return Arrays.equals(requested.getParameterTypes(), actual.getParameterTypes());
		}
		return false;
	}

	public static Object forwardImplemented(Method requested, Object[] args) throws InvocationTargetException, NoSuchMethodException {
		final Method matchedMethod = matchMethod(requested);
		if(matchedMethod != null) {
			try {
				return matchedMethod.invoke(INSTANCE, args);
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		throw new NoSuchMethodException("Method not found: " + requested.getName());
	}

	public static boolean isLevelEnabled(Level level) {
		return level.ordinal() <= OUTPUT_LEVEL.ordinal();
	}

	/**
	 * Interprets incoming method calls from one of the interfaces specified by CombinedLogger. This is the main entry point for logging, if the consumer is unaware of this implementing class.
	 *
	 * @return varies based on the method called, null if void was expected
	 * @throws Throwable dangerous!
	 * @see CombinedLogger
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/**
		 * see if the current level is greater than the requested level
		 * this isn't stupid. The library does a similar check
		 * @see org.slf4j.Logger#isEnabledForLevel(Level)
		 */
		if(method.getName().matches("is(Trace|Debug|Info|Warn|Error)Enabled")) {
			final String levelName = method.getName().substring(2).toUpperCase();
			final Level level = Level.valueOf(levelName);
			return isLevelEnabled(level);
		}

		if(method.getName().matches("trace|debug|info|warn|error")) {
			log(Level.valueOf(method.getName().toUpperCase()), args);
			return null;
		}

		try {
			return forwardImplemented(method, args);
		} catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public static String format(Object[] args) {
		if(args.length == 0)
			return "";

		args[0] = args[0].toString();
		if(args.length == 1)
			return (String) args[0];

		final FormattingTuple tuple;
		if(args.length == 2) {
			if(args[1] instanceof Object[]) {
				tuple = MessageFormatter.arrayFormat((String) args[0], (Object[]) args[1]);
			} else {
				tuple = MessageFormatter.format((String) args[0], args[1]);
			}
		} else if(args.length == 3) {
			if(args[1] instanceof Object[] && args[2] instanceof Throwable) {
				tuple = MessageFormatter.arrayFormat((String) args[0], (Object[]) args[1], (Throwable) args[2]);
			} else {
				tuple = MessageFormatter.format((String) args[0], args[1], args[2]);
			}
		} else {
			return Arrays.toString(args);
		}
		return tuple.getMessage();
	}

	@Override
	public Logger getLogger(String name) {
		return PROXY;
	}

}