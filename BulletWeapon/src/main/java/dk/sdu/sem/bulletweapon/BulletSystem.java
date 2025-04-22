package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
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

public class BulletSystem implements IUpdate, IGUIUpdate {
	private static final Logger LOGGER = Logger.getLogger(BulletSystem.class.getName());
	private static final boolean DEBUG = false;

	@Override
	public void update() {
		Set<BulletNode> bulletNodes = NodeManager.active().getNodes(BulletNode.class);
		if (bulletNodes.isEmpty()) return;

		List<Entity> bulletsToRemove = new ArrayList<>();

		for (BulletNode node : bulletNodes) {
			if (!isValidBullet(node)) continue;

			Entity bulletEntity = node.getEntity();

			// Check for hits via trigger listener
			BulletTriggerListener triggerListener = bulletEntity.getComponent(BulletTriggerListener.class);
			if (triggerListener != null && triggerListener.isHitDetected()) {
				bulletsToRemove.add(bulletEntity);
				continue;
			}

			// Update movement
			updateBulletMovement(node);

			// Check bounds
			if (isOutOfBounds(node.transform.getPosition())) {
				bulletsToRemove.add(bulletEntity);
			}
		}

		// Remove bullets marked for removal
		removeEntities(bulletsToRemove);
	}

	private boolean isValidBullet(BulletNode node) {
		return node != null &&
			node.transform != null &&
			node.bullet != null &&
			node.physics != null;
	}

	private void updateBulletMovement(BulletNode node) {
		Vector2D forward = node.transform.forward();
		float deltaTime = (float) Time.getDeltaTime();
		float speed = node.bullet.getSpeed();
		Vector2D velocity = forward.scale(speed);

		node.physics.setVelocity(velocity);
	}

	private boolean isOutOfBounds(Vector2D position) {
		float worldWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float worldHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();
		float margin = 50.0f;

		return position.x() < -margin ||
			position.x() > worldWidth + margin ||
			position.y() < -margin ||
			position.y() > worldHeight + margin;
	}

	private void removeEntities(List<Entity> entities) {
		for (Entity entity : entities) {
			if (entity.getScene() != null) {
				entity.getScene().removeEntity(entity);
				if (DEBUG) {
					LOGGER.log(Level.INFO, "Removed bullet: {0}", entity.getID());
				}
			}
		}
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		// Bullet trail effects or other visuals?
	}
}