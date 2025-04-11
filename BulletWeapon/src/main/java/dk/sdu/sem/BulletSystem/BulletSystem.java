package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * System for controlling bullet behavior and rendering.
 */
public class BulletSystem implements IUpdate, IGUIUpdate {
	private static final boolean DEBUG = false;

	@Override
	public void update() {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);
		Scene activeScene = Scene.getActiveScene();

		List<Entity> entitiesToRemove = new ArrayList<>();

		for (BulletNode bulletNode : bulletNodes) {
			// Get bullet entity and position
			Entity bulletEntity = bulletNode.getEntity();
			Vector2D currentPosition = bulletNode.transformComponent.getPosition();

			// Check for trigger-based collisions
			BulletTriggerListener triggerListener = bulletEntity.getComponent(BulletTriggerListener.class);
			if (triggerListener != null && triggerListener.isHitDetected()) {
				// Bullet hit something via the trigger system
				if (DEBUG) {
					Entity hitEntity = triggerListener.getHitEntity();
					System.out.println("Bullet hit entity: " +
						(hitEntity != null ? hitEntity.getID() : "unknown"));
				}

				entitiesToRemove.add(bulletEntity);
				continue;
			}

			// Move the bullet
			Vector2D forward = bulletNode.transformComponent.forward();
			float speed = (float)(bulletNode.bulletComponent.getSpeed() * Time.getDeltaTime());
			Vector2D movement = forward.scale(speed);
			bulletNode.transformComponent.translate(movement);

			// Check if bullet is out of bounds (based on world size)
			Vector2D position = bulletNode.transformComponent.getPosition();
			if (isOutOfBounds(position)) {
				// Queue for removal
				if (DEBUG) System.out.println("Bullet out of bounds, removing");
				entitiesToRemove.add(bulletEntity);
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
		// for bounds checking
		float worldWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float worldHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

		// Buffer margin so bullets disappear slightly outside the visible area
		float margin = 50.0f;

		return position.x() < -margin ||
			position.x() > worldWidth + margin ||
			position.y() < -margin ||
			position.y() > worldHeight + margin;
	}

	@Override
	public void onGUI(GraphicsContext gc) {

	}
}