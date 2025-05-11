package dk.sdu.sem.gamesystem.debug;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton manager for debug drawing operations
 */
public class DebugDrawingManager {
	private static final Logging LOGGER = Logging.createLogger("DebugDrawingManager", LoggingLevel.DEBUG);
	private static final DebugDrawingManager instance = new DebugDrawingManager();

	// Using CopyOnWriteArrayList for thread safety when rendering and adding rays concurrently
	private final List<DebugRay> rays = new CopyOnWriteArrayList<>();
	private final List<DebugCircle> circles = new CopyOnWriteArrayList<>();

	// How long debug rays persist (in seconds)
	private static final float DEFAULT_RAY_DURATION = 0.05f;  // Single frame at 60fps
	private boolean enabled = true;

	private DebugDrawingManager() {}

	public static DebugDrawingManager getInstance() {
		return instance;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		LOGGER.debug("Debug drawing " + (enabled ? "enabled" : "disabled"));
		if (!enabled) {
			clear();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Draw a ray from start point in the specified direction
	 */
	public void drawRay(Vector2D start, Vector2D direction, Color color) {
		if (!enabled) return;
		drawRay(start, direction, color, DEFAULT_RAY_DURATION);
	}

	/**
	 * Draw a ray from start point in the specified direction
	 * @param duration How long (in seconds) the ray should persist
	 */
	public void drawRay(Vector2D start, Vector2D direction, Color color, float duration) {
		if (!enabled) return;

		// Scale direction to make it visible
		float length = direction.magnitude();
		Vector2D end = start.add(direction);

		drawLine(start, end, color, duration);
	}

	/**
	 * Draw a line from start point to end point
	 */
	public void drawLine(Vector2D start, Vector2D end, Color color) {
		if (!enabled) return;
		drawLine(start, end, color, DEFAULT_RAY_DURATION);
	}

	/**
	 * Draw a line from start point to end point
	 * @param duration How long (in seconds) the line should persist
	 */
	public void drawLine(Vector2D start, Vector2D end, Color color, float duration) {
		if (!enabled) return;
		rays.add(new DebugRay(start, end, color, duration));
	}

	/**
	 * Draw a circle at the specified position
	 */
	public void drawCircle(Vector2D center, float radius, Color color, float duration) {
		if (!enabled) return;
		circles.add(new DebugCircle(center, radius, color, duration));
	}

	/**
	 * Update all debug shapes, removing expired ones
	 * @param deltaTime Time since last frame in seconds
	 */
	public void update(double deltaTime) {
		if (!enabled) return;
		updateList(rays, deltaTime);
		updateList(circles, deltaTime);
	}

	private <T extends DebugShape> void updateList(List<T> list, double deltaTime) {
		List<T> itemsToRemove = new ArrayList<>();

		for (T item : list) {
			item.remainingDuration -= deltaTime;
			if (item.remainingDuration <= 0) {
				itemsToRemove.add(item);
			}
		}

		list.removeAll(itemsToRemove);
	}

	/**
	 * Get all active debug rays
	 */
	public List<DebugRay> getRays() {
		return rays;
	}

	/**
	 * Get all active debug circles
	 */
	public List<DebugCircle> getCircles() {
		return circles;
	}

	/**
	 * Clear all debug shapes
	 */
	public void clear() {
		rays.clear();
		circles.clear();
	}

	/**
	 * Base class for debug shapes
	 */
	public static abstract class DebugShape {
		public final Color color;
		public float remainingDuration;

		public DebugShape(Color color, float duration) {
			this.color = color;
			this.remainingDuration = duration;
		}
	}

	/**
	 * Data class for a debug ray
	 */
	public static class DebugRay extends DebugShape {
		public final Vector2D start;
		public final Vector2D end;

		public DebugRay(Vector2D start, Vector2D end, Color color, float duration) {
			super(color, duration);
			this.start = start;
			this.end = end;
		}
	}

	/**
	 * Data class for a debug circle
	 */
	public static class DebugCircle extends DebugShape {
		public final Vector2D center;
		public final float radius;

		public DebugCircle(Vector2D center, float radius, Color color, float duration) {
			super(color, duration);
			this.center = center;
			this.radius = radius;
		}
	}
}