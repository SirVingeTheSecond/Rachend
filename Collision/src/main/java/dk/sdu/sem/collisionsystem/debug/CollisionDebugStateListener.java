package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.collisionsystem.CollisionServiceFactory;
import dk.sdu.sem.commonsystem.debug.IDebugController;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

public class CollisionDebugStateListener implements IDebugStateChangeListener {
	private static final Logging LOGGER = Logging.createLogger("CollisionDebugStateListener", LoggingLevel.DEBUG);

	@Override
	public void onDebugStateChanged() {
		// Use the static accessor method from the interface
		IDebugController controller = IDebugController.getInstance();

		// Check if debug visualization is enabled
		boolean colliderEnabled = controller.isColliderVisualizationEnabled();
		boolean raycastEnabled = controller.isRaycastVisualizationEnabled();
		boolean visualizationEnabled = colliderEnabled || raycastEnabled;

		LOGGER.debug("Debug state changed, visualization enabled: " + visualizationEnabled +
			" (collider: " + colliderEnabled + ", raycast: " + raycastEnabled + ")");

		// Update collision service debug state
		try {
			LOGGER.debug("Updating collision service debug state: " + visualizationEnabled);
			CollisionServiceFactory.setDebugEnabled(visualizationEnabled);
		} catch (Exception e) {
			LOGGER.error("Error updating collision service debug state: " + e.getMessage());
			e.printStackTrace();
		}
	}
}