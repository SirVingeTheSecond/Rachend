package dk.sdu.sem.debugrenderer;

import dk.sdu.sem.commonsystem.debug.IDebugController;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

public class DebugController implements IDebugController {
	private static final Logging LOGGER = Logging.createLogger("DebugController", LoggingLevel.DEBUG);

	// Use static fields for visualization state to ensure all instances share the same state
	private static final AtomicBoolean colliderVisualizationEnabled = new AtomicBoolean(false);
	private static final AtomicBoolean raycastVisualizationEnabled = new AtomicBoolean(false);
	private static final AtomicBoolean pathfindingVisualizationEnabled = new AtomicBoolean(false);

	public DebugController() {
		LOGGER.debug("DebugController instance created - current state: " +
			"collider=" + colliderVisualizationEnabled.get() +
			", raycast=" + raycastVisualizationEnabled.get() +
			", pathfinding=" + pathfindingVisualizationEnabled.get());
	}

	private void updateDebugDrawManager() {
		boolean anyEnabled = colliderVisualizationEnabled.get() ||
			raycastVisualizationEnabled.get() ||
			pathfindingVisualizationEnabled.get();

		// Use ServiceLoader to get DebugDrawManager
		ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.ifPresent(manager -> manager.setEnabled(anyEnabled));
	}

	@Override
	public void toggleColliderVisualization() {
		boolean newValue = !colliderVisualizationEnabled.get();
		colliderVisualizationEnabled.set(newValue);
		LOGGER.debug("Collider visualization toggled to: " + newValue);

		updateDebugDrawManager();
		notifyStateChanged();
	}

	@Override
	public void toggleRaycastVisualization() {
		boolean newValue = !raycastVisualizationEnabled.get();
		raycastVisualizationEnabled.set(newValue);
		LOGGER.debug("Raycast visualization toggled to: " + newValue);

		updateDebugDrawManager();
		notifyStateChanged();
	}

	@Override
	public void togglePathfindingVisualization() {
		boolean newValue = !pathfindingVisualizationEnabled.get();
		pathfindingVisualizationEnabled.set(newValue);
		LOGGER.debug("Pathfinding visualization toggled to: " + newValue);

		updateDebugDrawManager();
		notifyStateChanged();
	}

	@Override
	public boolean isColliderVisualizationEnabled() {
		return colliderVisualizationEnabled.get();
	}

	@Override
	public boolean isRaycastVisualizationEnabled() {
		return raycastVisualizationEnabled.get();
	}

	@Override
	public boolean isPathfindingVisualizationEnabled() {
		return pathfindingVisualizationEnabled.get();
	}

	@Override
	public void setColliderVisualizationEnabled(boolean enabled) {
		if (colliderVisualizationEnabled.get() != enabled) {
			colliderVisualizationEnabled.set(enabled);
			LOGGER.debug("Collider visualization set to: " + enabled);

			updateDebugDrawManager();
			notifyStateChanged();
		}
	}

	@Override
	public void setRaycastVisualizationEnabled(boolean enabled) {
		if (raycastVisualizationEnabled.get() != enabled) {
			raycastVisualizationEnabled.set(enabled);
			LOGGER.debug("Raycast visualization set to: " + enabled);

			updateDebugDrawManager();
			notifyStateChanged();
		}
	}

	@Override
	public void setPathfindingVisualizationEnabled(boolean enabled) {
		if (pathfindingVisualizationEnabled.get() != enabled) {
			pathfindingVisualizationEnabled.set(enabled);
			LOGGER.debug("Pathfinding visualization set to: " + enabled);

			updateDebugDrawManager();
			notifyStateChanged();
		}
	}

	private void notifyStateChanged() {
		LOGGER.debug("Notifying debug state change listeners. Current state: " +
			"collider=" + colliderVisualizationEnabled.get() +
			", raycast=" + raycastVisualizationEnabled.get() +
			", pathfinding=" + pathfindingVisualizationEnabled.get());

		ServiceLoader.load(IDebugStateChangeListener.class).forEach(listener -> {
			try {
				LOGGER.debug("Notifying listener: " + listener.getClass().getName());
				listener.onDebugStateChanged();
			} catch (Exception e) {
				LOGGER.error("Error notifying listener: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}
}