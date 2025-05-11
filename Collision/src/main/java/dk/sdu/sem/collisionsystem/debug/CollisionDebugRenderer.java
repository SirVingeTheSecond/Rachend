package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.debug.DebugDrawingManager;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollisionDebugRenderer implements IGUIUpdate {
	private static final Logging LOGGER = Logging.createLogger("CollisionDebugRenderer", LoggingLevel.DEBUG);

	private static boolean enabled = false;
	private static boolean showColliders = false;

	private final DebugDrawingManager debugDrawing = DebugDrawingManager.getInstance();

	@Override
	public void onGUI(GraphicsContext gc) {
		if (showColliders) {
			drawColliders(gc);
		}

		if (enabled) {
			drawDebugShapes(gc);
			debugDrawing.update(Time.getDeltaTime());
		}
	}

	private void drawDebugShapes(GraphicsContext gc) {
		drawDebugRays(gc);
		drawDebugCircles(gc);
	}

	private void drawDebugRays(GraphicsContext gc) {
		// Save original state
		double originalLineWidth = gc.getLineWidth();

		gc.setLineWidth(2.0);

		for (DebugDrawingManager.DebugRay ray : debugDrawing.getRays()) {
			gc.setStroke(ray.color);
			gc.strokeLine(
				ray.start.x(), ray.start.y(),
				ray.end.x(), ray.end.y()
			);

			// Draw a small circle at the hit point
			if (ray.color.getBrightness() > 0.7) {  // Brighter colors indicate hits
				gc.fillOval(ray.end.x() - 3, ray.end.y() - 3, 6, 6);
			}
		}

		// Restore original state
		gc.setLineWidth(originalLineWidth);
	}

	private void drawDebugCircles(GraphicsContext gc) {
		for (DebugDrawingManager.DebugCircle circle : debugDrawing.getCircles()) {
			gc.setFill(circle.color);
			gc.setStroke(circle.color.brighter());

			gc.fillOval(
				circle.center.x() - circle.radius,
				circle.center.y() - circle.radius,
				circle.radius * 2,
				circle.radius * 2
			);

			gc.strokeOval(
				circle.center.x() - circle.radius,
				circle.center.y() - circle.radius,
				circle.radius * 2,
				circle.radius * 2
			);
		}
	}

	// Your existing drawColliders method
	private void drawColliders(GraphicsContext gc) {
		try {
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);
			Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

			List<ColliderNode> safeNodeList = new ArrayList<>(colliderNodes);

			// Draw standard colliders
			gc.setStroke(Color.RED);
			gc.setLineWidth(1.0);
			gc.setGlobalAlpha(0.6);

			for (ColliderNode node : safeNodeList) {
				if (!NodeValidator.isColliderNodeValid(node)) continue;
				drawCollider(gc, node.transform, node.collider);
			}

			// Draw tilemap colliders
			gc.setStroke(Color.PURPLE);
			gc.setFill(new Color(1, 0, 1, 0.3));

			for (TilemapColliderNode node : tilemapNodes) {
				if (!NodeValidator.isTilemapNodeValid(node)) continue;
				drawTilemapCollider(gc, node);
			}

		} catch (Exception e) {
			LOGGER.error("Error in CollisionDebugRenderer: " + e.getMessage());
			e.printStackTrace();
		}

		gc.setGlobalAlpha(1.0);
	}

	private void drawCollider(GraphicsContext gc, TransformComponent transform, ColliderComponent collider) {
		Vector2D worldPos = transform.getPosition().add(collider.getOffset());
		ICollisionShape shape = collider.getShape();

		if (shape instanceof CircleShape) {
			drawCircleShape(gc, worldPos, (CircleShape)shape);
		}
		else if (shape instanceof BoxShape) {
			drawBoxShape(gc, worldPos, (BoxShape)shape);
		}
	}

	private void drawCircleShape(GraphicsContext gc, Vector2D pos, CircleShape circle) {
		float x = pos.x();
		float y = pos.y();
		float radius = circle.getRadius();

		gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

		// Draw cross at center
		float crossSize = 3;
		gc.strokeLine(x - crossSize, y, x + crossSize, y);
		gc.strokeLine(x, y - crossSize, x, y + crossSize);

		// Draw metadata
		gc.fillText("r = " + String.format("%.1f", radius), x + radius + 2, y);
	}

	private void drawBoxShape(GraphicsContext gc, Vector2D pos, BoxShape box) {
		gc.strokeRect(pos.x(), pos.y(), box.getWidth(), box.getHeight());
	}

	private void drawTilemapCollider(GraphicsContext gc, TilemapColliderNode node) {
		TransformComponent transform = node.transform;
		Vector2D position = transform.getPosition();
		int tileSize = node.tilemap.getTileSize();

		// Get the grid shape from the tilemap collider
		GridShape gridShape = (GridShape)node.collider.getShape();
		int[][] collisionFlags = gridShape.getCollisionFlags();
		int width = gridShape.getGridWidth();
		int height = gridShape.getGridHeight();

		// Draw solid tiles
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (gridShape.isSolid(x, y)) {
					float tileX = position.x() + (x * tileSize);
					float tileY = position.y() + (y * tileSize);

					gc.setGlobalAlpha(0.3);
					gc.fillRect(tileX, tileY, tileSize, tileSize);
					gc.setGlobalAlpha(0.7);
					gc.strokeRect(tileX, tileY, tileSize, tileSize);
				}
			}
		}
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
	}
}