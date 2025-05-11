package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.IDebugVisualizationSPI;
import dk.sdu.sem.collisionsystem.debug.CollisionDebugRenderer;
import dk.sdu.sem.gamesystem.debug.DebugDrawingManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Implementation of the IDebugVisualizationSPI for the Collision module.
 */
public class DebugVisualizationService implements IDebugVisualizationSPI {
	private static final Logging LOGGER = Logging.createLogger("DebugVisualizationService", LoggingLevel.DEBUG);

	@Override
	public void toggleDebugVisualization() {
		boolean currentState = CollisionDebugRenderer.isEnabled();
		boolean newState = !currentState;

		DebugDrawingManager.getInstance().setEnabled(newState);
		CollisionDebugRenderer.setEnabled(newState);

		// Reset the collision service to pick the appropriate implementation
		CollisionServiceFactory.reset();

		LOGGER.debug("Debug visualization " + (newState ? "enabled" : "disabled"));
	}

	@Override
	public void toggleColliderVisualization() {
		boolean currentState = CollisionDebugRenderer.isColliderVisualizationEnabled();
		boolean newState = !currentState;

		// Update the renderer
		CollisionDebugRenderer.setColliderVisualizationEnabled(newState);

		LOGGER.debug("Collider visualization " + (newState ? "enabled" : "disabled"));
	}

	@Override
	public void setDebugVisualizationEnabled(boolean enabled) {
		DebugDrawingManager.getInstance().setEnabled(enabled);
		CollisionDebugRenderer.setEnabled(enabled);

		// Reset the collision service to pick the appropriate implementation
		CollisionServiceFactory.reset();

		LOGGER.debug("Debug visualization " + (enabled ? "enabled" : "disabled"));
	}

	@Override
	public void setColliderVisualizationEnabled(boolean enabled) {
		CollisionDebugRenderer.setColliderVisualizationEnabled(enabled);
		LOGGER.debug("Collider visualization " + (enabled ? "enabled" : "disabled"));
	}

	@Override
	public boolean isDebugVisualizationEnabled() {
		return CollisionDebugRenderer.isEnabled();
	}

	@Override
	public boolean isColliderVisualizationEnabled() {
		return CollisionDebugRenderer.isColliderVisualizationEnabled();
	}
}