package dk.sdu.sem.debugrenderer;

import dk.sdu.sem.commonsystem.debug.*;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ServiceLoader;

public class DebugRenderer implements IGUIUpdate {
	private static final Logging LOGGER = Logging.createLogger("DebugRenderer", LoggingLevel.DEBUG);
	private static int frameCounter = 0;
	private static final int LOG_INTERVAL = 60; // Log every 60 frames

	private final IDebugController controller;
	private final IDebugDrawManager drawManager;

	public DebugRenderer() {
		this.controller = IDebugController.getInstance();

		this.drawManager = ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("No IDebugDrawManager implementation found"));

		LOGGER.debug("DebugRenderer initialized");
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		try {
			// Update frame counter for periodic logging
			frameCounter++;
			boolean shouldLog = frameCounter % LOG_INTERVAL == 0;

			if (shouldLog) {
				LOGGER.debug("DebugRenderer frame " + frameCounter +
					" - States: collider=" + controller.isColliderVisualizationEnabled() +
					", raycast=" + controller.isRaycastVisualizationEnabled() +
					", pathfinding=" + controller.isPathfindingVisualizationEnabled() +
					", drawManager enabled=" + drawManager.isEnabled());
			}

			// First update the debug manager to handle timed elements
			drawManager.update(Time.getDeltaTime());

			// Check if any visualization is enabled
			boolean anyVisualizationEnabled =
				controller.isColliderVisualizationEnabled() ||
					controller.isRaycastVisualizationEnabled() ||
					controller.isPathfindingVisualizationEnabled();

			// Always draw a debugging overlay to confirm the renderer is actually running
			if (anyVisualizationEnabled) {
				gc.setFill(Color.WHITE);
				gc.fillText("Debug Visualization Active", 10, 10);
			}

			// Render other debug drawings before?
			if (drawManager.isEnabled()) {
				//drawManager.drawAll(gc);
			}

			// Then, render visualizers based on enabled state
			if (anyVisualizationEnabled) {
				renderVisualizers(gc, shouldLog);
			}

		} catch (Exception e) {
			LOGGER.error("Error in DebugRenderer.onGUI: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void renderVisualizers(GraphicsContext gc, boolean shouldLog) {
		// Render collider visualizations
		if (controller.isColliderVisualizationEnabled()) {
			if (shouldLog) LOGGER.debug("Rendering collider visualizations");

			ServiceLoader<IColliderRenderer> colliderRenderers = ServiceLoader.load(IColliderRenderer.class);
			boolean found = false;

			for (IColliderRenderer visualizer : colliderRenderers) {
				found = true;
				if (shouldLog) LOGGER.debug("Found collider renderer: " + visualizer.getClass().getName());
				try {
					visualizer.drawColliders(gc);
				} catch (Exception e) {
					LOGGER.error("Error in collider visualizer: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!found && shouldLog) {
				LOGGER.warn("No IColliderRenderer implementations found");
			}
		}

		// Render raycast visualizations
		if (controller.isRaycastVisualizationEnabled()) {
			if (shouldLog) LOGGER.debug("Rendering raycast visualizations");

			ServiceLoader<IRaycastRenderer> raycastRenderers = ServiceLoader.load(IRaycastRenderer.class);
			boolean found = false;

			for (IRaycastRenderer visualizer : raycastRenderers) {
				found = true;
				if (shouldLog) LOGGER.debug("Found raycast renderer: " + visualizer.getClass().getName());
				try {
					visualizer.drawRaycasts(gc);
				} catch (Exception e) {
					LOGGER.error("Error in raycast visualizer: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!found && shouldLog) {
				LOGGER.warn("No IRaycastRenderer implementations found");
			}
		}

		// Render pathfinding visualizations
		if (controller.isPathfindingVisualizationEnabled()) {
			if (shouldLog) LOGGER.debug("Rendering pathfinding visualizations");

			ServiceLoader<IPathfindingRenderer> pathfindingRenderers = ServiceLoader.load(IPathfindingRenderer.class);
			boolean found = false;

			for (IPathfindingRenderer visualizer : pathfindingRenderers) {
				found = true;
				if (shouldLog) LOGGER.debug("Found pathfinding renderer: " + visualizer.getClass().getName());
				try {
					visualizer.drawPaths(gc);
				} catch (Exception e) {
					LOGGER.error("Error in pathfinding visualizer: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!found && shouldLog) {
				LOGGER.warn("No IPathfindingRenderer implementations found");
			}
		}
	}
}