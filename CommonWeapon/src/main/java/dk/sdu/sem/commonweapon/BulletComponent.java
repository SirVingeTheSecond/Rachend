package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component for bullets.
 */
public class BulletComponent implements IComponent {
	private final float speed;
	private final float damage;
	private final Entity owner;
	private boolean hasDamaged = false;

	/**
	 * Creates a bullet component.
	 *
	 * @param speed Projectile speed
	 * @param damage Damage amount
	 * @param owner Entity that fired the bullet
	 */
	public BulletComponent(float speed, float damage, Entity owner) {
		this.speed = speed;
		this.damage = damage;
		this.owner = owner;
	}

	public float getSpeed() {
		return speed;
	}

	public float getDamage() {
		return damage;
	}

	public Entity getOwner() {
		return owner;
	}

	public boolean hasDamaged() {
		return hasDamaged;
	}

	public void setDamaged(boolean damaged) {
		this.hasDamaged = damaged;
	}
}