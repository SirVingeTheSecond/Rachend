package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.RaycastResult;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.ServiceLocator;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * System for controlling bullet behavior and rendering.
 */
// This implementation is more of a hotfix for now
public class BulletSystem implements IUpdate, IGUIUpdate {
	@Override
	public void update() {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);
		Scene activeScene = Scene.getActiveScene();

		List<Entity> entitiesToRemove = new ArrayList<>();

		ICollisionSPI collisionService = ServiceLocator.getCollisionSystem();

		for (BulletNode bulletNode : bulletNodes) {
			// Get bullet entity and position
			Entity bulletEntity = bulletNode.getEntity();
			Vector2D currentPosition = bulletNode.transformComponent.getPosition();

			// Move the bullet
			Vector2D forward = bulletNode.transformComponent.forward();
			float speed = (float) (bulletNode.bulletComponent.getSpeed() * Time.getDeltaTime());
			Vector2D movement = forward.scale(speed);

			// Check if bullet would collide with any solid object
			boolean hitSolid = false;
			if (collisionService != null && bulletEntity.hasComponent(ColliderComponent.class)) {
				// Cast a ray to detect solid obstacles
				RaycastResult result = collisionService.raycast(
					currentPosition,
					forward,
					speed * 1.2f // Slightly longer than movement to ensure we detect hits
				);

				// If we hit something solid, remove the bullet
				if (result.isHit()) {
					entitiesToRemove.add(bulletEntity);
					hitSolid = true;
				}
			}

			// Only move the bullet if it didn't hit anything solid
			if (!hitSolid) {
				bulletNode.transformComponent.translate(movement);

				// Check if bullet is out of bounds (based on world size)
				Vector2D position = bulletNode.transformComponent.getPosition();
				if (isOutOfBounds(position)) {
					// Queue for removal
					entitiesToRemove.add(bulletEntity);
				}
			}
		}

		// Now remove all entities after iteration is complete
		for (Entity entity : entitiesToRemove) {
			if (entity.getScene() != null) {
				entity.getScene().removeEntity(entity);
			}
		}
	}

	private boolean isOutOfBounds(Vector2D position) {
		// Use the GameConstants world dimensions for bounds checking
		float worldWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float worldHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

		// Add a buffer margin so bullets disappear slightly outside the visible area
		float margin = 50.0f;

		return position.x() < -margin ||
			position.x() > worldWidth + margin ||
			position.y() < -margin ||
			position.y() > worldHeight + margin;
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);

		// Draw bullets
		gc.setFill(Color.YELLOW);
		for (BulletNode bulletNode : bulletNodes) {
			Vector2D position = bulletNode.transformComponent.getPosition();
			gc.fillOval(position.x() - 5, position.y() - 5, 10, 10);
		}
	}
}