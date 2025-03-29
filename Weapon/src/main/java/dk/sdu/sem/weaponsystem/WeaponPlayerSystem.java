package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

public class WeaponPlayerSystem implements IUpdate {

	///  Let the player shoot bullets with their weapon
	@Override
	public void update() {
		Set<WeaponPlayerNode> weaponPlayerNodes = NodeManager.active().getNodes(WeaponPlayerNode.class);

		// should only really be one node
		for (WeaponPlayerNode weaponPlayerNode : weaponPlayerNodes) {
			if (Input.getKeyDown(Key.MOUSE1)) {
				WeaponComponent weapon = weaponPlayerNode.weapon;

				weapon.tryShoot(() -> {
					Vector2D playerPosition = weaponPlayerNode.playerTransform.getPosition();
					Vector2D crosshairPosition = Input.getMousePosition();
					Vector2D direction = crosshairPosition.subtract(playerPosition).normalize();

					Entity entity = createBullet(weaponPlayerNode.playerTransform.getPosition(), direction.angle(), weapon);
					SceneManager.getInstance().getActiveScene().addEntity(entity);
				});
			}
		}
	}

	/// Create a new bullet entity given the position, rotation and weapon
	private Entity createBullet(Vector2D position, float rotation, WeaponComponent weapon) {
		Entity entity = new Entity();
		entity.addComponent(new BulletComponent(1, 100.0f));
		entity.addComponent(new TransformComponent(position, rotation));
		return entity;
	}
}