package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.weaponsystem.WeaponComponent;
import dk.sdu.sem.weaponsystem.WeaponPlayerNode;

import java.util.Set;

public class BulletWeaponSystem implements IUpdate {

//	///  Let the player shoot bullets with their weapon
	// needs to reworked
	@Override
	public void update() {
//		Set<BulletWeapon> weaponPlayerNodes =
//			NodeManager.active().getNodes(BulletWeapon.class);
//
//		// should only really be one node
//		for (BulletWeapon weaponPlayerNode : weaponPlayerNodes) {
//			if (Input.getKeyDown(Key.MOUSE1)) {
//				BulletNode weapon = BulletWeapon.Activate;
//
//				weapon.tryShoot(() -> {
//					Vector2D playerPosition = weaponPlayerNode.playerTransform.getPosition();
//					Vector2D crosshairPosition = Input.getMousePosition();
//					Vector2D direction = crosshairPosition.subtract(playerPosition).normalize();
//
//					Entity entity = createBullet(weaponPlayerNode.playerTransform.getPosition(), direction.angle(), weapon);
//					SceneManager.getInstance().getActiveScene().addEntity(entity);
//				});
//			}
//		}
	}

	/// Create a new bullet entity given the position, rotation and weapon
	private Entity createBullet(Vector2D position, float rotation, WeaponComponent weapon) {
		Entity entity = new Entity();
		entity.addComponent(new BulletComponent(1, 100.0f));
		entity.addComponent(new TransformComponent(position, rotation));
		return entity;
	}
}