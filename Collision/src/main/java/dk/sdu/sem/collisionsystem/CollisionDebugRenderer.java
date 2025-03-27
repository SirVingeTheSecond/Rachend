package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ColliderComponent;
import dk.sdu.sem.collision.CircleShape;
import dk.sdu.sem.collision.ICollisionShape;
import dk.sdu.sem.collision.RectangleShape;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

/**
 * Debug renderer for collision shapes to visualize colliders.
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
		// Get collider nodes from the active NodeManager
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Set drawing style for colliders
		gc.setStroke(Color.RED);
		gc.setLineWidth(1.0);
		gc.setGlobalAlpha(0.6);

		// Draw each collider
		for (ColliderNode node : colliderNodes) {
			TransformComponent transform = node.transform;
			ColliderComponent collider = node.collider;
			ICollisionShape shape = collider.getCollisionShape();

			// Get world position (transform position + collider offset)
			Vector2D worldPos = transform.getPosition().add(collider.getOffset());
			float x = worldPos.x();
			float y = worldPos.y();

			// Draw based on shape type
			if (shape instanceof CircleShape circle) {
				float radius = circle.getRadius();

				// Draw the circle
				gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

				// Draw cross at center
				float crossSize = 3;
				gc.strokeLine(x - crossSize, y, x + crossSize, y);
				gc.strokeLine(x, y - crossSize, x, y + crossSize);

				// Draw circle metadata
				gc.fillText("r=" + String.format("%.1f", radius), x + radius + 2, y);
			}
			else if (shape instanceof RectangleShape) {
				RectangleShape rect = (RectangleShape) shape;
				float width = rect.getWidth();
				float height = rect.getHeight();

				// Draw the rectangle
				gc.strokeRect(x, y, width, height);
			}
		}

		// Draw tilemap colliders
		drawTilemapColliders(gc);

		// Reset global alpha
		gc.setGlobalAlpha(1.0);
	}

	private void drawTilemapColliders(GraphicsContext gc) {
		// Get tilemap collider nodes
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

		// Set drawing style for tilemap colliders
		gc.setStroke(Color.PURPLE);
		gc.setFill(new Color(1, 0, 1, 0.3)); // Cool semi-transparent purple :D

		// Draw each tilemap collider
		for (TilemapColliderNode node : tilemapNodes) {
			TransformComponent transform = node.transform;
			Vector2D position = transform.getPosition();
			int tileSize = node.tilemap.getTileSize();

			// Get collision flags
			int[][] collisionFlags = node.tilemapCollider.getCollisionFlags();
			int width = collisionFlags.length;
			int height = collisionFlags[0].length;

			// Draw solid tiles
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (collisionFlags[x][y] == 1) { // 1 = solid
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

		// Reset global alpha
		gc.setGlobalAlpha(1.0);
	}
}