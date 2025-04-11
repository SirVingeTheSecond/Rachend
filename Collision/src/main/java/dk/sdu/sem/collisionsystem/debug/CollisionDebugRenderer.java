package dk.sdu.sem.collisionsystem.debug;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Debug renderer to visualize colliders.
 */
public class CollisionDebugRenderer implements IGUIUpdate {
	// Toggle debug rendering
	private static final boolean ENABLED = true;

	@Override
	public void onGUI(GraphicsContext gc) {
		if (!ENABLED) return;

		// Draw all colliders
		drawColliders(gc);
	}

	private void drawColliders(GraphicsContext gc) {
		try {
			// Get collider nodes from the active NodeManager
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

			// IMPORTANT: Make a copy of the nodes to avoid ConcurrentModificationException
			List<ColliderNode> safeNodeList = new ArrayList<>(colliderNodes);

			// Set drawing style for colliders
			gc.setStroke(Color.RED);
			gc.setLineWidth(1.0);
			gc.setGlobalAlpha(0.6);

			// Draw each collider
			for (ColliderNode node : safeNodeList) {
				// Skip processing if entity is null or has been removed from scene
				if (node.getEntity() == null || node.getEntity().getScene() == null) {
					continue;
				}

				TransformComponent transform = node.transform;
				ColliderComponent collider = node.collider;

				// Skip if components are null
				if (transform == null || collider == null) {
					continue;
				}

				ICollisionShape shape = collider.getShape();
				if (shape == null) {
					continue;
				}

				// Get world position (transform position + collider offset)
				Vector2D worldPos = transform.getPosition().add(collider.getOffset());
				float x = worldPos.x();
				float y = worldPos.y();

				// Draw based on shape type
				if (shape instanceof CircleShape) {
					CircleShape circle = (CircleShape) shape;
					float radius = circle.getRadius();

					// Draw the circle
					gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

					// Draw cross at center
					float crossSize = 3;
					gc.strokeLine(x - crossSize, y, x + crossSize, y);
					gc.strokeLine(x, y - crossSize, x, y + crossSize);

					// Draw circle metadata
					gc.fillText("r = " + String.format("%.1f", radius), x + radius + 2, y);
				}
				else if (shape instanceof BoxShape box) {
					float width = box.getWidth();
					float height = box.getHeight();

					// Draw the rectangle
					gc.strokeRect(x, y, width, height);
				}
			}

			// Draw tilemap colliders
			drawTilemapColliders(gc);

		} catch (Exception e) {
			System.err.println("Error in CollisionDebugRenderer: " + e.getMessage());
			e.printStackTrace();
		}

		// Reset global alpha
		gc.setGlobalAlpha(1.0);
	}

	private void drawTilemapColliders(GraphicsContext gc) {
		try {
			// Get all collider nodes and filter to only those with tilemaps
			Set<ColliderNode> allColliderNodes = NodeManager.active().getNodes(ColliderNode.class);
			List<ColliderNode> tilemapNodes = allColliderNodes.stream()
				.filter(ColliderNode::isTilemapCollider)
				.toList();

			// Set drawing style for tilemap colliders
			gc.setStroke(Color.PURPLE);
			gc.setFill(new Color(1, 0, 1, 0.3)); // Cool semi-transparent purple

			// Draw each tilemap collider
			for (ColliderNode node : tilemapNodes) {
				// Skip if entity is null or has been removed
				if (node.getEntity() == null || node.getEntity().getScene() == null) {
					continue;
				}

				TransformComponent transform = node.transform;
				if (transform == null || node.tilemap == null) {
					continue;
				}

				Vector2D position = transform.getPosition();
				int tileSize = node.tilemap.getTileSize();

				// Get collision flags from the GridShape
				GridShape gridShape = node.getGridShape();
				if (gridShape == null) {
					continue;
				}

				int[][] collisionFlags = gridShape.getCollisionFlags();
				int width = gridShape.getGridWidth();
				int height = gridShape.getGridHeight();

				// Draw solid tiles
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						if (gridShape.isSolid(x, y)) { // Using the GridShape method
							float tileX = position.x() + (x * tileSize);
							float tileY = position.y() + (y * tileSize);

							// Draw solid tile
							gc.setGlobalAlpha(0.3);
							gc.fillRect(tileX, tileY, tileSize, tileSize);
							gc.setGlobalAlpha(0.7);
							gc.strokeRect(tileX, tileY, tileSize, tileSize);
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error in drawTilemapColliders: " + e.getMessage());
			e.printStackTrace();
		}

		// Reset global alpha
		gc.setGlobalAlpha(1.0);
	}
}