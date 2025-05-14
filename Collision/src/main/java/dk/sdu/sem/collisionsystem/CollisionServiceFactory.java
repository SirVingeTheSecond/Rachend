package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Factory for obtaining collision service instances.
 */
public class CollisionServiceFactory implements IDebugStateChangeListener {
	private static final Logging LOGGER = Logging.createLogger("CollisionServiceFactory", LoggingLevel.DEBUG);

	private static ICollisionSPI instance;
	private static ICollisionSPI baseInstance;
	private static DebugCollisionService debugInstance;
	private static boolean debugEnabled = false;

	/**
	 * Gets the appropriate collision service based on debug settings.
	 */
	public static synchronized ICollisionSPI getService() {
		if (instance == null) {
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
		LOGGER.debug("Resetting collision service (Debug enabled: " + debugEnabled + ")");
		instance = null;
	}

	/**
	 * Set debug visualization enabled state
	 */
	public static synchronized void setDebugEnabled(boolean enabled) {
		if (debugEnabled != enabled) {
			debugEnabled = enabled;
			LOGGER.debug("Debug collision service " + (enabled ? "enabled" : "disabled"));

			// If we have a debug instance already and it's active, update its state
			if (debugInstance != null && instance == debugInstance) {
				debugInstance.setDebugEnabled(enabled);
			}

			// Reset to apply changes
			reset();
		}
	}

	/**
	 * Check if debug is enabled
	 */
	public static boolean isDebugEnabled() {
		return debugEnabled;
	}
}