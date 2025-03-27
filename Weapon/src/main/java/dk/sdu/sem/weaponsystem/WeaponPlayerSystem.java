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

	@Override
	public void update() {
		Set<WeaponPlayerNode> weaponPlayerNodes = NodeManager.active().getNodes(WeaponPlayerNode.class);

		// should only really be one node
		for (WeaponPlayerNode weaponPlayerNode : weaponPlayerNodes) {
			if (Input.getKeyDown(Key.MOUSE1)) {
				System.out.println("Shooting");
				WeaponComponent weapon = weaponPlayerNode.weapon;

				weapon.tryShoot(() -> {
					Vector2D playerPosition = weaponPlayerNode.playerTransform.getPosition();
					Vector2D crosshairPosition = Input.getMousePosition();
					Vector2D direction = crosshairPosition.subtract(playerPosition).normalize();

					System.out.println("direction = " + direction);
					System.out.println("playerPosition = " + playerPosition);
					System.out.println("crosshairPosition = " + crosshairPosition);

					Entity entity = createBullet(weaponPlayerNode.playerTransform.getPosition(), direction.angle(), weapon);
					SceneManager.getInstance().getActiveScene().addEntity(entity);
				});
			}
		}
	}

	private Entity createBullet(Vector2D position, float rotation, WeaponComponent weapon) {
		Entity entity = new Entity();
		entity.addComponent(new BulletComponent(1, 100.0f));
		entity.addComponent(new TransformComponent(position, rotation));
		return entity;
	}
}