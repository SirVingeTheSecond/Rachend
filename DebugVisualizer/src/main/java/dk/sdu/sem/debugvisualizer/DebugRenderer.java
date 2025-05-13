package dk.sdu.sem.debugvisualizer;

import dk.sdu.sem.commonsystem.debug.IColliderVisualizer;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;

import java.util.ServiceLoader;

/**
 * Renders debug visualizations like colliders, rays, and circles.
 */
public class DebugRenderer implements IGUIUpdate {
	private static final Logging LOGGER = Logging.createLogger("DebugRenderer", LoggingLevel.DEBUG);

	private static boolean enabled = false;
	private static boolean showColliders = false;

	// Get the debug manager through ServiceLoader
	private final IDebugDrawManager debugManager;

	public DebugRenderer() {
		this.debugManager = ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.orElse(null);

		if (this.debugManager == null) {
			LOGGER.error("Failed to get IDebugDrawManager instance - debug visualizations will be disabled");
		} else {
			LOGGER.debug("DebugRenderer initialized with debug manager: " + debugManager.getClass().getName());
		}
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		try {
			if (debugManager == null) return;

			if (showColliders) {
				drawColliders(gc);
			}

			if (enabled) {
				drawDebugShapes(gc);
				debugManager.update(Time.getDeltaTime());
			}
		} catch (Exception e) {
			LOGGER.error("Error in DebugRenderer.onGUI: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void drawDebugShapes(GraphicsContext gc) {
		try {
			drawDebugRays(gc);
			drawDebugCircles(gc);
		} catch (Exception e) {
			LOGGER.error("Error drawing debug shapes: " + e.getMessage());
		}
	}

	private void drawDebugRays(GraphicsContext gc) {
		// Save original state
		double originalLineWidth = gc.getLineWidth();

		gc.setLineWidth(2.0);

		int rayCount = 0;
		for (IDebugDrawManager.DebugRayInfo ray : debugManager.getRays()) {
			gc.setStroke(ray.getColor());
			gc.strokeLine(
				ray.getStart().x(), ray.getStart().y(),
				ray.getEnd().x(), ray.getEnd().y()
			);

			// Draw a small circle at the hit point
			if (ray.getColor().getBrightness() > 0.7) {  // Brighter colors indicate hits
				gc.fillOval(ray.getEnd().x() - 3, ray.getEnd().y() - 3, 6, 6);
			}
			rayCount++;
		}

		if (rayCount > 0) {
			LOGGER.debug("Drew " + rayCount + " debug rays");
		}

		// Restore original state
		gc.setLineWidth(originalLineWidth);
	}

	private void drawDebugCircles(GraphicsContext gc) {
		int circleCount = 0;
		for (IDebugDrawManager.DebugCircleInfo circle : debugManager.getCircles()) {
			gc.setFill(circle.getColor());
			gc.setStroke(circle.getColor().brighter());

			gc.fillOval(
				circle.getCenter().x() - circle.getRadius(),
				circle.getCenter().y() - circle.getRadius(),
				circle.getRadius() * 2,
				circle.getRadius() * 2
			);

			gc.strokeOval(
				circle.getCenter().x() - circle.getRadius(),
				circle.getCenter().y() - circle.getRadius(),
				circle.getRadius() * 2,
				circle.getRadius() * 2
			);
			circleCount++;
		}

		if (circleCount > 0) {
			LOGGER.debug("Drew " + circleCount + " debug circles");
		}
	}

	private void drawColliders(GraphicsContext gc) {
		try {
			ServiceLoader.load(IColliderVisualizer.class).forEach(visualizer -> {
				try {
					visualizer.drawColliders(gc);
				} catch (Exception e) {
					LOGGER.error("Error in collider visualizer: " + e.getMessage());
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error in DebugRenderer.drawColliders: " + e.getMessage());
			e.printStackTrace();
		}

		gc.setGlobalAlpha(1.0);
	}

	/**
	 * Check if debug visualization is enabled
	 */
	public static boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set debug visualization enabled state
	 */
	public static void setEnabled(boolean value) {
		enabled = value;
		// Ensure the debug manager state is synchronized via ServiceLoader
		ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.ifPresent(manager -> manager.setEnabled(value));
		LOGGER.debug("DebugRenderer enabled set to: " + value);
	}

	/**
	 * Check if collider visualization is enabled
	 */
	public static boolean isColliderVisualizationEnabled() {
		return showColliders;
	}

	/**
	 * Set collider visualization enabled state
	 */
	public static void setColliderVisualizationEnabled(boolean value) {
		showColliders = value;
		LOGGER.debug("Collider visualization enabled set to: " + value);
	}
}