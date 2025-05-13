package dk.sdu.sem.debugvisualizer;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IDebugDrawManager.
 * Manages debug drawings like rays and circles.
 */
public class DebugDrawManager implements IDebugDrawManager {
	private static final Logging LOGGER = Logging.createLogger("DebugDrawManager", LoggingLevel.DEBUG);

	// Singleton instance
	private static DebugDrawManager instance;

	private boolean enabled = false;
	private final List<DebugRay> rays = new ArrayList<>();
	private final List<DebugCircle> circles = new ArrayList<>();

	// Public constructor required for ServiceLoader
	public DebugDrawManager() {
		LOGGER.debug("Creating DebugDrawManager instance");
		// If we're creating a new instance via ServiceLoader but the singleton already exists
		if (instance != null) {
			// Copy state from the existing instance to maintain consistency
			this.enabled = instance.enabled;
			this.rays.addAll(instance.rays);
			this.circles.addAll(instance.circles);
			LOGGER.debug("New instance created by ServiceLoader, synchronized state with singleton");
		}
		// Update the singleton reference to this instance
		instance = this;
	}

	/**
	 * Gets the singleton instance of DebugDrawManager.
	 */
	public static synchronized DebugDrawManager getInstance() {
		if (instance == null) {
			instance = new DebugDrawManager();
		}
		return instance;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled) {
			clear();
		}
		LOGGER.debug("DebugDrawManager enabled: " + enabled);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void clear() {
		LOGGER.debug("Clearing debug drawings (" + rays.size() + " rays, " + circles.size() + " circles)");
		rays.clear();
		circles.clear();
	}

	@Override
	public void update(double deltaTime) {
		if (!enabled) return;

		// Update rays and remove expired ones
		int raysRemoved = 0;
		Iterator<DebugRay> rayIterator = rays.iterator();
		while (rayIterator.hasNext()) {
			DebugRay ray = rayIterator.next();
			ray.duration -= (float) deltaTime;
			if (ray.duration <= 0) {
				rayIterator.remove();
				raysRemoved++;
			}
		}

		// Update circles and remove expired ones
		int circlesRemoved = 0;
		Iterator<DebugCircle> circleIterator = circles.iterator();
		while (circleIterator.hasNext()) {
			DebugCircle circle = circleIterator.next();
			circle.duration -= (float) deltaTime;
			if (circle.duration <= 0) {
				circleIterator.remove();
				circlesRemoved++;
			}
		}

		if (raysRemoved > 0 || circlesRemoved > 0) {
			LOGGER.debug("Removed " + raysRemoved + " rays and " + circlesRemoved +
				" circles. Remaining: " + rays.size() + " rays, " + circles.size() + " circles");
		}
	}

	@Override
	public void drawRay(Vector2D start, Vector2D direction, Color color, float duration) {
		if (!enabled) return;
		Vector2D end = start.add(direction);
		rays.add(new DebugRay(start, end, color, duration));
		LOGGER.debug("Added ray from " + start + " to " + end + " (duration: " + duration + "s)");
	}

	@Override
	public void drawLine(Vector2D start, Vector2D end, Color color, float duration) {
		if (!enabled) return;
		rays.add(new DebugRay(start, end, color, duration));
		LOGGER.debug("Added line from " + start + " to " + end + " (duration: " + duration + "s)");
	}

	@Override
	public void drawCircle(Vector2D center, float radius, Color color, float duration) {
		if (!enabled) return;
		circles.add(new DebugCircle(center, radius, color, duration));
		LOGGER.debug("Added circle at " + center + " with radius " + radius + " (duration: " + duration + "s)");
	}

	@Override
	public List<DebugRayInfo> getRays() {
		return new ArrayList<>(rays);
	}

	@Override
	public List<DebugCircleInfo> getCircles() {
		return new ArrayList<>(circles);
	}

	/**
	 * Implementation of DebugRayInfo interface
	 */
	public static class DebugRay implements DebugRayInfo {
		private final Vector2D start;
		private final Vector2D end;
		private final Color color;
		private float duration;

		public DebugRay(Vector2D start, Vector2D end, Color color, float duration) {
			this.start = start;
			this.end = end;
			this.color = color;
			this.duration = duration;
		}

		@Override
		public Vector2D getStart() {
			return start;
		}

		@Override
		public Vector2D getEnd() {
			return end;
		}

		@Override
		public Color getColor() {
			return color;
		}

		@Override
		public float getDuration() {
			return duration;
		}
	}

	/**
	 * Implementation of DebugCircleInfo interface
	 */
	public static class DebugCircle implements DebugCircleInfo {
		private final Vector2D center;
		private final float radius;
		private final Color color;
		private float duration;

		public DebugCircle(Vector2D center, float radius, Color color, float duration) {
			this.center = center;
			this.radius = radius;
			this.color = color;
			this.duration = duration;
		}

		@Override
		public Vector2D getCenter() {
			return center;
		}

		@Override
		public float getRadius() {
			return radius;
		}

		@Override
		public Color getColor() {
			return color;
		}

		@Override
		public float getDuration() {
			return duration;
		}
	}
}