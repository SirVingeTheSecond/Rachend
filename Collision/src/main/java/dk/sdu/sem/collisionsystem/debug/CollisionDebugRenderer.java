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
import dk.sdu.sem.commonsystem.debug.IColliderVisualizer;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Renders collider visualizations for debugging.
 */
public class CollisionDebugRenderer implements IColliderVisualizer {
	private static final Logging LOGGER = Logging.createLogger("CollisionDebugRenderer", LoggingLevel.DEBUG);

	private final IDebugDrawManager debugDrawManager;

	public CollisionDebugRenderer() {
		this.debugDrawManager = ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.orElse(null);

		if (this.debugDrawManager == null) {
			LOGGER.error("Failed to get IDebugDrawManager instance - debug visualizations will be disabled");
		} else {
			LOGGER.debug("CollisionDebugRenderer initialized with debug manager: " + debugDrawManager.getClass().getName());
		}
	}

	@Override
	public void drawColliders(GraphicsContext gc) {
		try {
			LOGGER.debug("Drawing collision visualization");
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
			LOGGER.error("Error drawing colliders: " + e.getMessage());
			e.printStackTrace();
		}
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
}