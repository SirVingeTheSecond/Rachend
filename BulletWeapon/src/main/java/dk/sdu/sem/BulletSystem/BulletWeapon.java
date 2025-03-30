package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonweapon.IWeapon;
public class BulletWeapon implements IWeapon {
	// spawn a BulletNode, which can be observed BulletSystem
	@Override
	public void activateWeapon(Entity activator) {
		BulletNode bulletNode = new BulletNode();
		// TODO offset this spawned bullet from player location a bit.
	}

}
