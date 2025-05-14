package dk.sdu.sem.debugrenderer;

import dk.sdu.sem.commonsystem.debug.IColliderRenderer;
import dk.sdu.sem.commonsystem.debug.IPathfindingRenderer;
import dk.sdu.sem.commonsystem.debug.IRaycastRenderer;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;

import java.util.ServiceLoader;

public class DebugRenderer implements IGUIUpdate {
	private static final Logging LOGGER = Logging.createLogger("DebugRenderer", LoggingLevel.DEBUG);

	private final DebugController controller;
	private final DebugDrawManager drawManager;

	public DebugRenderer() {
		this.controller = DebugController.getInstance();
		this.drawManager = DebugDrawManager.getInstance();
		LOGGER.debug("DebugRenderer initialized");
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		try {
			// First update the debug manager to handle timed elements
			drawManager.update(Time.getDeltaTime());

			// Render primitive debug drawings if any visualization is enabled
			if (controller.isColliderVisualizationEnabled() ||
				controller.isRaycastVisualizationEnabled() ||
				controller.isPathfindingVisualizationEnabled()) {

				drawManager.drawAll(gc);
			}

			// Then, render visualizers based on enabled state
			renderVisualizers(gc);

		} catch (Exception e) {
			LOGGER.error("Error in DebugRenderer.onGUI: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void renderVisualizers(GraphicsContext gc) {
		// Render collider visualizations
		if (controller.isColliderVisualizationEnabled()) {
			LOGGER.debug("Rendering collider visualizations");
			ServiceLoader<IColliderRenderer> colliderRenderers = ServiceLoader.load(IColliderRenderer.class);
			boolean found = false;

			for (IColliderRenderer visualizer : colliderRenderers) {
				found = true;
				LOGGER.debug("Found collider renderer: " + visualizer.getClass().getName());
				try {
					visualizer.drawColliders(gc);
				} catch (Exception e) {
					LOGGER.error("Error in collider visualizer: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!found) {
				LOGGER.warn("No IColliderRenderer implementations found");
			}
		}

		// Render raycast visualizations
		if (controller.isRaycastVisualizationEnabled()) {
			LOGGER.debug("Rendering raycast visualizations");
			ServiceLoader<IRaycastRenderer> raycastRenderers = ServiceLoader.load(IRaycastRenderer.class);
			boolean found = false;

			for (IRaycastRenderer visualizer : raycastRenderers) {
				found = true;
				LOGGER.debug("Found raycast renderer: " + visualizer.getClass().getName());
				try {
					visualizer.drawRaycasts(gc);
				} catch (Exception e) {
					LOGGER.error("Error in raycast visualizer: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!found) {
				LOGGER.warn("No IRaycastRenderer implementations found");
			}
		}

		// Render pathfinding visualizations
		if (controller.isPathfindingVisualizationEnabled()) {
			LOGGER.debug("Rendering pathfinding visualizations");
			ServiceLoader<IPathfindingRenderer> pathfindingRenderers = ServiceLoader.load(IPathfindingRenderer.class);
			boolean found = false;

			for (IPathfindingRenderer visualizer : pathfindingRenderers) {
				found = true;
				LOGGER.debug("Found pathfinding renderer: " + visualizer.getClass().getName());
				try {
					visualizer.drawPaths(gc);
				} catch (Exception e) {
					LOGGER.error("Error in pathfinding visualizer: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!found) {
				LOGGER.warn("No IPathfindingRenderer implementations found");
			}
		}
	}
}