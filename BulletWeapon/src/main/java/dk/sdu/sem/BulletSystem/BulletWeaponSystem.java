//package dk.sdu.sem.BulletSystem;
//
//import dk.sdu.sem.commonsystem.NodeManager;
//import dk.sdu.sem.gamesystem.Time;
//import dk.sdu.sem.gamesystem.services.IUpdate;
//
//import java.util.Set;
//
//public class BulletWeaponSystem implements IUpdate {
////	/// Update weapons cooldown timer
//	@Override
//	public void update() {
//		// get the Get every instance of BulletWeapon, this could be done via
//		// a factory which automatically adds them to a list available
//		// externally
////		Set<WeaponNode> weaponNodes = NodeManager.active().getNodes(WeaponNode.class);
////
////		for (WeaponNode weaponNode : weaponNodes) {
////			WeaponComponent weapon = weaponNode.weapon;
//
////			weapon.timer -= (float) Time.getDeltaTime();
////			weapon.timer = Math.max(0, weapon.timer);
////		}
//	}
//}
