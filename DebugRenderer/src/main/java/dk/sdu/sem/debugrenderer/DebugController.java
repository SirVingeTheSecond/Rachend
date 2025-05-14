package dk.sdu.sem.debugrenderer;

import dk.sdu.sem.commonsystem.debug.IDebugController;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ServiceLoader;

public class DebugController implements IDebugController {
	private static final Logging LOGGER = Logging.createLogger("DebugController", LoggingLevel.DEBUG);

	// Visualization state
	private boolean colliderVisualizationEnabled = false;
	private boolean raycastVisualizationEnabled = false;
	private boolean pathfindingVisualizationEnabled = false;

	// Singleton instance for service loading
	private static final DebugController INSTANCE = new DebugController();

	public static DebugController getInstance() {
		return INSTANCE;
	}

	public DebugController() {
		// Singleton constructor
	}

	@Override
	public void toggleColliderVisualization() {
		colliderVisualizationEnabled = !colliderVisualizationEnabled;
		LOGGER.debug("Collider visualization toggled to: " + colliderVisualizationEnabled);

		// Sync with debug draw manager - enable if ANY visualization is enabled
		DebugDrawManager.getInstance().setEnabled(
			colliderVisualizationEnabled || raycastVisualizationEnabled || pathfindingVisualizationEnabled);

		notifyStateChanged();
	}

	@Override
	public void toggleRaycastVisualization() {
		raycastVisualizationEnabled = !raycastVisualizationEnabled;
		LOGGER.debug("Raycast visualization toggled to: " + raycastVisualizationEnabled);

		// Sync with debug draw manager - enable if ANY visualization is enabled
		DebugDrawManager.getInstance().setEnabled(
			colliderVisualizationEnabled || raycastVisualizationEnabled || pathfindingVisualizationEnabled);

		notifyStateChanged();
	}

	@Override
	public void togglePathfindingVisualization() {
		pathfindingVisualizationEnabled = !pathfindingVisualizationEnabled;
		LOGGER.debug("Pathfinding visualization toggled to: " + pathfindingVisualizationEnabled);

		// Sync with debug draw manager - enable if ANY visualization is enabled
		DebugDrawManager.getInstance().setEnabled(
			colliderVisualizationEnabled || raycastVisualizationEnabled || pathfindingVisualizationEnabled);

		notifyStateChanged();
	}

	@Override
	public boolean isColliderVisualizationEnabled() {
		return colliderVisualizationEnabled;
	}

	@Override
	public boolean isRaycastVisualizationEnabled() {
		return raycastVisualizationEnabled;
	}

	@Override
	public boolean isPathfindingVisualizationEnabled() {
		return pathfindingVisualizationEnabled;
	}

	@Override
	public void setColliderVisualizationEnabled(boolean enabled) {
		if (colliderVisualizationEnabled != enabled) {
			colliderVisualizationEnabled = enabled;
			LOGGER.debug("Collider visualization set to: " + enabled);

			// Sync with debug draw manager
			DebugDrawManager.getInstance().setEnabled(
				colliderVisualizationEnabled || raycastVisualizationEnabled || pathfindingVisualizationEnabled);

			notifyStateChanged();
		}
	}

	@Override
	public void setRaycastVisualizationEnabled(boolean enabled) {
		if (raycastVisualizationEnabled != enabled) {
			raycastVisualizationEnabled = enabled;
			LOGGER.debug("Raycast visualization set to: " + enabled);

			// Sync with debug draw manager
			DebugDrawManager.getInstance().setEnabled(
				colliderVisualizationEnabled || raycastVisualizationEnabled || pathfindingVisualizationEnabled);

			notifyStateChanged();
		}
	}

	@Override
	public void setPathfindingVisualizationEnabled(boolean enabled) {
		if (pathfindingVisualizationEnabled != enabled) {
			pathfindingVisualizationEnabled = enabled;
			LOGGER.debug("Pathfinding visualization set to: " + enabled);

			// Sync with debug draw manager
			DebugDrawManager.getInstance().setEnabled(
				colliderVisualizationEnabled || raycastVisualizationEnabled || pathfindingVisualizationEnabled);

			notifyStateChanged();
		}
	}

	private void notifyStateChanged() {
		LOGGER.debug("Notifying debug state change listeners");
		ServiceLoader.load(IDebugStateChangeListener.class).forEach(listener -> {
			try {
				listener.onDebugStateChanged();
			} catch (Exception e) {
				LOGGER.error("Error notifying listener: " + e.getMessage());
			}
		});
	}
}