package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
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
//			if (weaponPlayerNode.player.isShooting()) {
			if (Input.getKeyDown(Key.SPACE)) {
				System.out.println("Shooting");
				WeaponComponent weapon = weaponPlayerNode.weapon;

				weapon.tryShoot(() -> {
					System.out.println("Creating bullet");
					Entity entity = BulletFactory.createBullet(weaponPlayerNode.playerTransform, weapon);
					SceneManager.getInstance().getActiveScene().addEntity(entity);
				});
			}
		}
	}
}