package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

public class CollisionServiceFactory implements IDebugStateChangeListener {
	private static final Logging LOGGER = Logging.createLogger("CollisionServiceFactory", LoggingLevel.DEBUG);

	private static ICollisionSPI instance;
	private static ICollisionSPI baseInstance;
	private static DebugCollisionService debugInstance;
	private static boolean debugEnabled = false;

	public static synchronized ICollisionSPI getService() {
		if (instance == null) {
			LOGGER.debug("Initializing collision service (debug=" + debugEnabled + ")");

			if (baseInstance == null) {
				baseInstance = new CollisionService();
				LOGGER.debug("Created base collision service: " + baseInstance.getClass().getName());
			}

			if (debugEnabled) {
				if (debugInstance == null) {
					debugInstance = new DebugCollisionService(baseInstance);
					LOGGER.debug("Created debug collision service: " + debugInstance.getClass().getName());
				} else {
					debugInstance.setDebugEnabled(true);
					LOGGER.debug("Reusing existing debug service and enabling debug");
				}
				instance = debugInstance;
			} else {
				if (debugInstance != null) {
					debugInstance.setDebugEnabled(false);
					LOGGER.debug("Disabling existing debug service");
				}
				instance = baseInstance;
			}

			LOGGER.debug("Returning collision service: " + instance.getClass().getName());
		}

		return instance;
	}

	@Override
	public void onDebugStateChanged() {
		LOGGER.debug("Debug state changed, resetting collision service");
		reset();
	}

	public static synchronized void reset() {
		LOGGER.debug("Resetting collision service (Debug enabled: " + debugEnabled + ")");
		instance = null;
	}

	public static synchronized void setDebugEnabled(boolean enabled) {
		if (debugEnabled != enabled) {
			debugEnabled = enabled;
			LOGGER.debug("Debug collision service " + (enabled ? "enabled" : "disabled"));

			// If we have a debug instance already and it's active, update its state
			if (debugInstance != null) {
				debugInstance.setDebugEnabled(enabled);
				LOGGER.debug("Updated existing debug service state to: " + enabled);
			}

			// Reset to apply changes
			reset();
		}
	}

	public static boolean isDebugEnabled() {
		return debugEnabled;
	}
}