package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.collisionsystem.CollisionServiceFactory;
import dk.sdu.sem.commonsystem.debug.IDebugController;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ServiceLoader;

public class CollisionDebugStateListener implements IDebugStateChangeListener {
	private static final Logging LOGGER = Logging.createLogger("CollisionDebugStateListener", LoggingLevel.DEBUG);

	@Override
	public void onDebugStateChanged() {
		// Check if debug visualization is enabled from the controller
		boolean visualizationEnabled = isVisualizationEnabled();

		LOGGER.debug("Debug state changed, visualization enabled: " + visualizationEnabled);

		// Update collision service debug state
		try {
			LOGGER.debug("Updating collision service debug state: " + visualizationEnabled);
			CollisionServiceFactory.setDebugEnabled(visualizationEnabled);
		} catch (Exception e) {
			LOGGER.error("Error updating collision service debug state: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean isVisualizationEnabled() {
		return ServiceLoader.load(IDebugController.class)
			.findFirst()
			.map(controller -> {
				boolean colliderEnabled = controller.isColliderVisualizationEnabled();
				boolean raycastEnabled = controller.isRaycastVisualizationEnabled();
				boolean enabled = colliderEnabled || raycastEnabled;

				LOGGER.debug("Visualization enabled check: " + enabled +
					" (collider: " + colliderEnabled +
					", raycast: " + raycastEnabled + ")");

				return enabled;
			})
			.orElse(false);
	}
}