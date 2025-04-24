package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component for entities that can use weapons.
 */
public class WeaponComponent implements IComponent {
	private final IWeaponSPI weapon;
	private final float damage;
	private final double attackCooldown;
	private double lastActivatedTime = 0;
	private float attackSize;

	/**
	 * Creates a weapon component.
	 *
	 * @param weapon The weapon implementation
	 * @param damage Base damage for this weapon
	 * @param attackCooldown Time between attacks in seconds
	 */
	public WeaponComponent(IWeaponSPI weapon, float damage, double attackCooldown) {
		this.weapon = weapon;
		this.damage = damage;
		this.attackCooldown = attackCooldown;
	}

	public IWeaponSPI getWeapon() {
		return weapon;
	}

	public float getDamage() {
		return damage;
	}

	public double getAttackCooldown() {
		return attackCooldown;
	}

	public double getLastActivatedTime() {
		return lastActivatedTime;
	}

	public void setLastActivatedTime(double time) {
		this.lastActivatedTime = time;
	}

	/**
	 * Checks if weapon can fire based on cooldown.
	 */
	public boolean canFire(double currentTime) {
		return currentTime - lastActivatedTime >= attackCooldown;
	}

	public float getAttackSize() {
		return this.attackSize;
	}
}