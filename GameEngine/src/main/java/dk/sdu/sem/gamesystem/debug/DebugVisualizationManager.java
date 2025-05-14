package dk.sdu.sem.gamesystem.debug;

import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.paint.Color;

/**
 * Singleton manager for connecting debug visualization systems across modules
 */
public class DebugVisualizationManager implements IDebugDrawManager {
	private static final Logging LOGGER = Logging.createLogger("DebugVisualizationManager", LoggingLevel.DEBUG);

	private static final DebugVisualizationManager instance = new DebugVisualizationManager();

	private final DebugDrawingManager drawingManager = DebugDrawingManager.getInstance();

	private DebugVisualizationManager() {}

	public static DebugVisualizationManager getInstance() {
		return instance;
	}

	@Override
	public void drawRay(Vector2D start, Vector2D direction, Color color, float duration) {
		drawingManager.drawRay(start, direction, color, duration);
	}

	@Override
	public void drawLine(Vector2D start, Vector2D end, Color color, float duration) {
		drawingManager.drawLine(start, end, color, duration);
	}

	@Override
	public void drawCircle(Vector2D center, float radius, Color color, float duration) {
		drawingManager.drawCircle(center, radius, color, duration);
	}

	@Override
	public void setEnabled(boolean enabled) {
		drawingManager.setEnabled(enabled);
	}

	@Override
	public boolean isEnabled() {
		return drawingManager.isEnabled();
	}

	@Override
	public void clear() {
		drawingManager.clear();
	}

	@Override
	public void update(double deltaTime) {
		drawingManager.update(deltaTime);
	}

	@Override
	public List<DebugRayInfo> getRays() {
		// Convert internal ray format to interface format
		return drawingManager.getRays().stream()
			.map(ray -> new DebugRayInfoImpl(ray.start, ray.end, ray.color, ray.remainingDuration))
			.collect(Collectors.toList());
	}

	@Override
	public List<DebugCircleInfo> getCircles() {
		// Convert internal circle format to interface format
		return drawingManager.getCircles().stream()
			.map(circle -> new DebugCircleInfoImpl(circle.center, circle.radius,
				circle.color, circle.remainingDuration))
			.collect(Collectors.toList());
	}

	// Implementation classes for the info interfaces
	private static class DebugRayInfoImpl implements DebugRayInfo {
		private final Vector2D start;
		private final Vector2D end;
		private final Color color;
		private final float duration;

		public DebugRayInfoImpl(Vector2D start, Vector2D end, Color color, float duration) {
			this.start = start;
			this.end = end;
			this.color = color;
			this.duration = duration;
		}

		@Override public Vector2D getStart() { return start; }
		@Override public Vector2D getEnd() { return end; }
		@Override public Color getColor() { return color; }
		@Override public float getDuration() { return duration; }
	}

	private static class DebugCircleInfoImpl implements DebugCircleInfo {
		private final Vector2D center;
		private final float radius;
		private final Color color;
		private final float duration;

		public DebugCircleInfoImpl(Vector2D center, float radius, Color color, float duration) {
			this.center = center;
			this.radius = radius;
			this.color = color;
			this.duration = duration;
		}

		@Override public Vector2D getCenter() { return center; }
		@Override public float getRadius() { return radius; }
		@Override public Color getColor() { return color; }
		@Override public float getDuration() { return duration; }
	}
}