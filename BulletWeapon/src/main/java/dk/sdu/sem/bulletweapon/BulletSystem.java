package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.BulletTriggerListener;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for controlling bullets.
 */
public class BulletSystem implements IUpdate, IGUIUpdate {
	private static final Logger LOGGER = Logger.getLogger(BulletSystem.class.getName());
	private static final boolean DEBUG = false;

	@Override
	public void update() {
		Set<BulletNode> projectileNodes = NodeManager.active().getNodes(BulletNode.class);

		if (projectileNodes.isEmpty()) {
			return;
		}

		List<Entity> entitiesToRemove = new ArrayList<>();

		for (BulletNode node : projectileNodes) {
			// Get projectile entity
			Entity projectileEntity = node.getEntity();

			// Check for trigger-based hits
			BulletTriggerListener triggerListener = projectileEntity.getComponent(BulletTriggerListener.class);
			if (triggerListener != null && triggerListener.isHitDetected()) {
				// Projectile hit something via the trigger system
				if (DEBUG) {
					Entity hitEntity = triggerListener.getHitEntity();
					LOGGER.log(Level.INFO, "Projectile hit entity: {0}",
						(hitEntity != null ? hitEntity.getID() : "unknown"));
				}

				entitiesToRemove.add(projectileEntity);
				continue;
			}

			// Move the projectile
			Vector2D forward = node.transform.forward();
			float deltaTime = (float) Time.getDeltaTime();
			float speed = node.bullet.getSpeed() * deltaTime;
			Vector2D movement = forward.scale(speed);
			node.transform.translate(movement);

			// Check if projectile is out of bounds
			Vector2D position = node.transform.getPosition();
			if (isOutOfBounds(position)) {
				if (DEBUG) LOGGER.info("Projectile out of bounds, removing");
				entitiesToRemove.add(projectileEntity);
			}
		}

		// Remove all entities after iteration is complete
		removeEntities(entitiesToRemove);
	}

	/**
	 * Removes a list of entities from the scene.
	 */
	private void removeEntities(List<Entity> entities) {
		for (Entity entity : entities) {
			if (entity.getScene() != null) {
				entity.getScene().removeEntity(entity);

				if (DEBUG) {
					LOGGER.log(Level.INFO, "Removed projectile: {0}", entity.getID());
				}
			}
		}
	}

	/**
	 * Checks if a position is outside the world boundaries.
	 */
	private boolean isOutOfBounds(Vector2D position) {
		// Calculate world dimensions
		float worldWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float worldHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

		// Buffer margin so projectiles disappear slightly outside the visible area
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