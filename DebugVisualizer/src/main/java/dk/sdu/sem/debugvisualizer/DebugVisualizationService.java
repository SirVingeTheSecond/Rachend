package dk.sdu.sem.debugvisualizer;

import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.commonsystem.debug.IDebugStateChangeListener;
import dk.sdu.sem.commonsystem.debug.IDebugVisualizationSPI;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ServiceLoader;

/**
 * Implementation of the IDebugVisualizationSPI for the Debug Visualizer module.
 */
public class DebugVisualizationService implements IDebugVisualizationSPI {
	private static final Logging LOGGER = Logging.createLogger("DebugVisualizationService", LoggingLevel.DEBUG);

	@Override
	public void toggleDebugVisualization() {
		boolean currentState = DebugRenderer.isEnabled();
		boolean newState = !currentState;

		// Update debug manager state through ServiceLoader
		ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.ifPresent(manager -> manager.setEnabled(newState));

		// Update renderer state
		DebugRenderer.setEnabled(newState);

		// Notify modules that use this SPI
		notifyDebugStateChanged();

		LOGGER.debug("Debug visualization " + (newState ? "enabled" : "disabled"));
	}

	@Override
	public void toggleColliderVisualization() {
		boolean currentState = DebugRenderer.isColliderVisualizationEnabled();
		boolean newState = !currentState;

		// Update the renderer
		DebugRenderer.setColliderVisualizationEnabled(newState);

		LOGGER.debug("Collider visualization " + (newState ? "enabled" : "disabled"));
	}

	@Override
	public void setDebugVisualizationEnabled(boolean enabled) {
		// Update debug manager state through ServiceLoader
		ServiceLoader.load(IDebugDrawManager.class)
			.forEach(manager -> manager.setEnabled(enabled));

		// Update renderer state
		DebugRenderer.setEnabled(enabled);

		// Notify modules that use this SPI
		notifyDebugStateChanged();

		LOGGER.debug("Debug visualization " + (enabled ? "enabled" : "disabled"));
	}

	@Override
	public void setColliderVisualizationEnabled(boolean enabled) {
		DebugRenderer.setColliderVisualizationEnabled(enabled);
		LOGGER.debug("Collider visualization " + (enabled ? "enabled" : "disabled"));
	}

	@Override
	public boolean isDebugVisualizationEnabled() {
		return DebugRenderer.isEnabled();
	}

	@Override
	public boolean isColliderVisualizationEnabled() {
		return DebugRenderer.isColliderVisualizationEnabled();
	}

	/**
	 * Notify other modules that might need to know when debug state changes
	 */
	private void notifyDebugStateChanged() {
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