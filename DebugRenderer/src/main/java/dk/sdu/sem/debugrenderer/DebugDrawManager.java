package dk.sdu.sem.debugrenderer;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages primitive debug drawing operations like lines, circles, etc.
 */
public class DebugDrawManager implements IDebugDrawManager {
	private static final Logging LOGGER = Logging.createLogger("DebugDrawManager", LoggingLevel.DEBUG);
	private static DebugDrawManager instance;

	private boolean enabled = false;

	// Drawing element collections
	private final List<DebugRay> rays = new ArrayList<>();
	private final List<DebugLine> lines = new ArrayList<>();
	private final List<DebugCircle> circles = new ArrayList<>();
	private final List<DebugRect> rects = new ArrayList<>();
	private final List<DebugText> texts = new ArrayList<>();

	// Constructor for ServiceLoader
	public DebugDrawManager() {
		LOGGER.debug("Creating DebugDrawManager instance");
		// If we're creating a new instance via ServiceLoader but the singleton already exists
		if (instance != null) {
			// Copy state from existing instance for consistency
			this.enabled = instance.enabled;
			this.rays.addAll(instance.rays);
			this.lines.addAll(instance.lines);
			this.circles.addAll(instance.circles);
			this.rects.addAll(instance.rects);
			this.texts.addAll(instance.texts);
			LOGGER.debug("New instance created by ServiceLoader, synchronized state with singleton");
		}
		// Update the singleton reference to this instance
		instance = this;
	}

	/**
	 * Gets the singleton instance.
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
		LOGGER.debug("Clearing debug drawings (" + rays.size() + " rays, " +
			lines.size() + " lines, " +
			circles.size() + " circles, " +
			rects.size() + " rects, " +
			texts.size() + " texts)");
		rays.clear();
		lines.clear();
		circles.clear();
		rects.clear();
		texts.clear();
	}

	@Override
	public void update(double deltaTime) {
		if (!enabled) return;

		updateCollection(rays, deltaTime);
		updateCollection(lines, deltaTime);
		updateCollection(circles, deltaTime);
		updateCollection(rects, deltaTime);
		updateCollection(texts, deltaTime);
	}

	/**
	 * Updates a collection of debug elements, removing expired ones.
	 */
	private <T extends DebugElement> void updateCollection(List<T> collection, double deltaTime) {
		int elementsRemoved = 0;
		Iterator<T> iterator = collection.iterator();
		while (iterator.hasNext()) {
			T element = iterator.next();
			element.remainingTime -= (float) deltaTime;
			if (element.remainingTime <= 0) {
				iterator.remove();
				elementsRemoved++;
			}
		}

		if (elementsRemoved > 0) {
			LOGGER.debug("Removed " + elementsRemoved + " elements from collection. Remaining: " + collection.size());
		}
	}

	@Override
	public void drawRay(Vector2D start, Vector2D direction, Color color, float duration) {
		if (!enabled) return;
		rays.add(new DebugRay(start, direction, color, duration));
	}

	@Override
	public void drawLine(Vector2D start, Vector2D end, Color color, float duration) {
		if (!enabled) return;
		lines.add(new DebugLine(start, end, color, duration));
	}

	@Override
	public void drawCircle(Vector2D center, float radius, Color color, float duration) {
		if (!enabled) return;
		circles.add(new DebugCircle(center, radius, color, duration));
	}

	@Override
	public void drawRect(Vector2D position, float width, float height, Color color, float duration) {
		if (!enabled) return;
		rects.add(new DebugRect(position, width, height, color, duration));
	}

	@Override
	public void drawText(String text, Vector2D position, Color color, float duration) {
		if (!enabled) return;
		texts.add(new DebugText(text, position, color, duration));
	}

	/**
	 * Draws all debug elements using the provided graphics context.
	 */
	public void drawAll(GraphicsContext gc) {
		if (!enabled) return;

		for (DebugRay ray : rays) ray.draw(gc);
		for (DebugLine line : lines) line.draw(gc);
		for (DebugCircle circle : circles) circle.draw(gc);
		for (DebugRect rect : rects) rect.draw(gc);
		for (DebugText text : texts) text.draw(gc);
	}

	// Base class for all debug elements
	private abstract static class DebugElement {
		protected final Color color;
		protected float remainingTime;
		protected final float duration;

		protected DebugElement(Color color, float duration) {
			this.color = color;
			this.duration = duration;
			this.remainingTime = duration;
		}

		/**
		 * Draws this element using the graphics context.
		 */
		abstract void draw(GraphicsContext gc);
	}

	/**
	 * Ray debug element
	 */
	private static class DebugRay extends DebugElement {
		private final Vector2D start;
		private final Vector2D direction;

		public DebugRay(Vector2D start, Vector2D direction, Color color, float duration) {
			super(color, duration);
			this.start = start;
			this.direction = direction;
		}

		@Override
		void draw(GraphicsContext gc) {
			Vector2D end = start.add(direction);
			gc.setStroke(color);
			gc.setLineWidth(2.0);
			gc.strokeLine(start.x(), start.y(), end.x(), end.y());

			// Draw arrowhead to indicate direction
			double arrowSize = 5.0;
			Vector2D dir = direction.normalize();
			Vector2D perpendicular = new Vector2D(-dir.y(), dir.x());

			Vector2D arrowLeft = end.subtract(dir.scale((float)arrowSize))
				.add(perpendicular.scale((float)arrowSize * 0.5f));
			Vector2D arrowRight = end.subtract(dir.scale((float)arrowSize))
				.subtract(perpendicular.scale((float)arrowSize * 0.5f));

			gc.strokeLine(end.x(), end.y(), arrowLeft.x(), arrowLeft.y());
			gc.strokeLine(end.x(), end.y(), arrowRight.x(), arrowRight.y());
		}
	}

	/**
	 * Line debug element
	 */
	private static class DebugLine extends DebugElement {
		private final Vector2D start;
		private final Vector2D end;

		public DebugLine(Vector2D start, Vector2D end, Color color, float duration) {
			super(color, duration);
			this.start = start;
			this.end = end;
		}

		@Override
		void draw(GraphicsContext gc) {
			gc.setStroke(color);
			gc.setLineWidth(1.5);
			gc.strokeLine(start.x(), start.y(), end.x(), end.y());
		}
	}

	/**
	 * Circle debug element
	 */
	private static class DebugCircle extends DebugElement {
		private final Vector2D center;
		private final float radius;

		public DebugCircle(Vector2D center, float radius, Color color, float duration) {
			super(color, duration);
			this.center = center;
			this.radius = radius;
		}

		@Override
		void draw(GraphicsContext gc) {
			gc.setStroke(color);
			gc.setFill(color.deriveColor(0, 1, 1, 0.2));

			double x = center.x() - radius;
			double y = center.y() - radius;
			double diameter = radius * 2;

			gc.fillOval(x, y, diameter, diameter);
			gc.strokeOval(x, y, diameter, diameter);
		}
	}

	/**
	 * Rectangle debug element
	 */
	private static class DebugRect extends DebugElement {
		private final Vector2D position;
		private final float width;
		private final float height;

		public DebugRect(Vector2D position, float width, float height, Color color, float duration) {
			super(color, duration);
			this.position = position;
			this.width = width;
			this.height = height;
		}

		@Override
		void draw(GraphicsContext gc) {
			gc.setStroke(color);
			gc.setFill(color.deriveColor(0, 1, 1, 0.2));

			gc.fillRect(position.x(), position.y(), width, height);
			gc.strokeRect(position.x(), position.y(), width, height);
		}
	}

	/**
	 * Text debug element
	 */
	private static class DebugText extends DebugElement {
		private final String text;
		private final Vector2D position;

		public DebugText(String text, Vector2D position, Color color, float duration) {
			super(color, duration);
			this.text = text;
			this.position = position;
		}

		@Override
		void draw(GraphicsContext gc) {
			gc.setFill(color);
			gc.fillText(text, position.x(), position.y());
		}
	}
}