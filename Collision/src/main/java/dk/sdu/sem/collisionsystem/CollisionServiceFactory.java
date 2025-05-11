package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.IDebugVisualizationSPI;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ServiceLoader;

/**
 * Factory for obtaining collision service instances.
 */
public class CollisionServiceFactory {
	private static final Logging LOGGER = Logging.createLogger("CollisionServiceFactory", LoggingLevel.DEBUG);
	private static ICollisionSPI instance;

	/**
	 * Gets the appropriate collision service based on debug settings.
	 * Uses DebugCollisionService when debug visualization is enabled,
	 * otherwise uses the base CollisionService.
	 *
	 * @return The collision service instance
	 */
	public static synchronized ICollisionSPI getService() {
		if (instance == null) {
			boolean debugEnabled = isDebugEnabled();
			LOGGER.debug("Initializing collision service (debug=" + debugEnabled + ")");

			if (debugEnabled) {
				instance = new DebugCollisionService(getBaseService());
				LOGGER.debug("Using debug collision service");
			} else {
				instance = getBaseService();
				LOGGER.debug("Using base collision service");
			}
		}

		return instance;
	}

	/**
	 * Resets the singleton instance.
	 * Call this when debug settings change to get a new instance.
	 */
	public static synchronized void reset() {
		instance = null;
	}

	/**
	 * Gets the base collision service implementation.
	 */
	private static ICollisionSPI getBaseService() {
		return new CollisionService();
	}

	/**
	 * Checks if debug visualization is enabled.
	 */
	private static boolean isDebugEnabled() {
		return ServiceLoader.load(IDebugVisualizationSPI.class)
			.findFirst()
			.map(IDebugVisualizationSPI::isDebugVisualizationEnabled)
			.orElse(false);
	}
}