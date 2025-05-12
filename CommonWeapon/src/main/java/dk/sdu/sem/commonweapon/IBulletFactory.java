package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

public interface IBulletFactory {
	Entity createBullet(Vector2D position, Vector2D direction, WeaponComponent weaponComponent, Entity owner);
}
