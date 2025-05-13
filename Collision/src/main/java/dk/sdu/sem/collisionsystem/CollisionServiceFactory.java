package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.commonsystem.debug.IDebugVisualizationSPI;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ServiceLoader;

/**
 * Factory for obtaining collision service instances.
 */
public class CollisionServiceFactory implements IDebugStateChangeListener {
	private static final Logging LOGGER = Logging.createLogger("CollisionServiceFactory", LoggingLevel.DEBUG);
	private static ICollisionSPI instance;
	private static ICollisionSPI baseInstance;
	private static DebugCollisionService debugInstance;

	/**
	 * Gets the appropriate collision service based on debug settings.
	 */
	public static synchronized ICollisionSPI getService() {
		if (instance == null) {
			boolean debugEnabled = isDebugEnabled();
			LOGGER.debug("Initializing collision service (debug=" + debugEnabled + ")");

			// Create base instance if it doesn't exist
			if (baseInstance == null) {
				baseInstance = new CollisionService();
				LOGGER.debug("Created base collision service");
			}

			if (debugEnabled) {
				// Create debug instance if it doesn't exist or reuse existing one
				if (debugInstance == null) {
					debugInstance = new DebugCollisionService(baseInstance);
					LOGGER.debug("Created debug collision service");
				}
				instance = debugInstance;
			} else {
				instance = baseInstance;
			}
		}

		return instance;
	}

	/**
	 * Called when debug state changes.
	 */
	@Override
	public void onDebugStateChanged() {
		LOGGER.debug("Debug state changed, resetting collision service");
		reset();
	}

	/**
	 * Resets the singleton instance.
	 */
	public static synchronized void reset() {
		LOGGER.debug("Resetting collision service (Debug enabled: " + isDebugEnabled() + ")");
		instance = null;
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