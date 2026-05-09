package xyz.cliserkad.timber;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;

/**
 * Merges SLF4J's {@link Logger} and Maven's {@link Log} into a single interface,
 * allowing {@link Lumberjack}'s dynamic proxy to satisfy both consumers without branching.
 */
public interface CombinedLogger extends Logger, Log {

}
