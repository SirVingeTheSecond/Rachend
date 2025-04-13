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
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollisionDebugRenderer implements IGUIUpdate {
	private static final boolean ENABLED = true;

	@Override
	public void onGUI(GraphicsContext gc) {
		if (!ENABLED) return;

		/*
		// Debug stuff
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);
		System.out.println("CollisionDebugRenderer found: " + colliderNodes.size() +
			" collider nodes and " + tilemapNodes.size() + " tilemap nodes");
 		*/

		drawColliders(gc);
	}

	private void drawColliders(GraphicsContext gc) {
		try {
			// Get ALL collider nodes
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);
			Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

			// Copy to avoid concurrent modification
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
			System.err.println("Error in CollisionDebugRenderer: " + e.getMessage());
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
}