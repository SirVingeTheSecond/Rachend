package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that represents a bullet entity.
 */
public class BulletComponent implements IComponent {
	private int speed;
	private int damage;
	private Entity owner; // To prevent self-damage

	/**
	 * Creates a default bullet with speed 40 and damage 10.
	 */
	public BulletComponent() {
		this(40, 10, null);
	}

	/**
	 * Creates a bullet with specified parameters.
	 *
	 * @param speed The bullet's movement speed
	 * @param damage The amount of damage the bullet does
	 * @param owner The entity that fired the bullet (to prevent self-damage)
	 */
	public BulletComponent(int speed, int damage, Entity owner) {
		this.speed = speed;
		this.damage = damage;
		this.owner = owner;
	}

	/**
	 * Gets the bullet's speed.
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * Gets the bullet's damage amount.
	 */
	public int getDamage() {
		return damage;
	}

	/**
	 * Gets the entity that fired this bullet.
	 */
	public Entity getOwner() {
		return owner;
	}
}