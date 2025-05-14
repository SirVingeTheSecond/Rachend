package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.*;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.debug.IColliderRenderer;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

/**
 * Renders collider visualizations for debugging.
 */
public class CollisionDebugRenderer implements IColliderRenderer {
	private static final Logging LOGGER = Logging.createLogger("CollisionDebugRenderer", LoggingLevel.DEBUG);

	private static final Color COLLIDER_COLOR = Color.RED;
	private static final Color TRIGGER_COLOR = Color.YELLOW;
	private static final Color TILEMAP_COLOR = Color.PURPLE;
	private static final Color LABEL_COLOR = Color.WHITE;

	@Override
	public void drawColliders(GraphicsContext gc) {
		try {
			LOGGER.debug("Drawing collision visualization");
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);
			Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

			LOGGER.debug("Found " + colliderNodes.size() + " collider nodes and " +
				tilemapNodes.size() + " tilemap nodes");

			if (colliderNodes.isEmpty() && tilemapNodes.isEmpty()) {
				// Draw a message if no colliders found
				gc.setFill(Color.WHITE);
				gc.fillText("No colliders found in scene", 10, 40);
				return;
			}

			// Draw standard colliders
			gc.setLineWidth(1.0);
			gc.setGlobalAlpha(0.6);

			for (ColliderNode node : colliderNodes) {
				if (!NodeValidator.isColliderNodeValid(node)) continue;
				drawCollider(gc, node.transform, node.collider);
			}

			// Draw tilemap colliders
			for (TilemapColliderNode node : tilemapNodes) {
				if (!NodeValidator.isTilemapNodeValid(node)) continue;
				drawTilemapCollider(gc, node);
			}

			// Reset alpha
			gc.setGlobalAlpha(1.0);

		} catch (Exception e) {
			LOGGER.error("Error drawing colliders: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void drawCollider(GraphicsContext gc, TransformComponent transform, ColliderComponent collider) {
		Vector2D worldPos = transform.getPosition().add(collider.getOffset());
		ICollisionShape shape = collider.getShape();

		// Use different colors based on whether this is a trigger
		if (collider.isTrigger()) {
			gc.setStroke(TRIGGER_COLOR);
			gc.setLineWidth(1.5);
		} else {
			gc.setStroke(COLLIDER_COLOR);
			gc.setLineWidth(1.0);
		}

		if (shape instanceof CircleShape) {
			drawCircleShape(gc, worldPos, (CircleShape)shape);
		}
		else if (shape instanceof BoxShape) {
			drawBoxShape(gc, worldPos, (BoxShape)shape);
		}

		// Draw collision layer information
		String layerName = collider.getLayer().toString();
		gc.setFill(LABEL_COLOR);
		gc.fillText(layerName, worldPos.x(), worldPos.y() - 5);
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
		float width = box.getWidth();
		float height = box.getHeight();

		gc.strokeRect(pos.x(), pos.y(), width, height);

		// Draw diagonal cross to show center
		gc.strokeLine(pos.x(), pos.y(), pos.x() + width, pos.y() + height);
		gc.strokeLine(pos.x() + width, pos.y(), pos.x(), pos.y() + height);

		// Draw size info
		gc.fillText(String.format("%.1fx%.1f", width, height), pos.x(), pos.y() - 5);
	}

	private void drawTilemapCollider(GraphicsContext gc, TilemapColliderNode node) {
		TransformComponent transform = node.transform;
		Vector2D position = transform.getPosition();
		int tileSize = node.tilemap.getTileSize();

		// Get the grid shape from the tilemap collider
		GridShape gridShape = node.getGridShape();
		int[][] collisionFlags = gridShape.getCollisionFlags();
		int width = gridShape.getGridWidth();
		int height = gridShape.getGridHeight();

		// Draw layer info
		gc.setFill(LABEL_COLOR);
		gc.fillText("Layer: " + node.collider.getLayer().toString(), position.x(), position.y() - 10);

		// Draw grid boundaries
		gc.setStroke(TILEMAP_COLOR);
		gc.setLineWidth(0.5);
		gc.strokeRect(position.x(), position.y(), width * tileSize, height * tileSize);

		// Draw solid tiles
		gc.setFill(TILEMAP_COLOR.deriveColor(0, 1, 1, 0.3));
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (gridShape.isSolid(x, y)) {
					float tileX = position.x() + (x * tileSize);
					float tileY = position.y() + (y * tileSize);

					gc.fillRect(tileX, tileY, tileSize, tileSize);
					gc.strokeRect(tileX, tileY, tileSize, tileSize);
				}
			}
		}
	}
}