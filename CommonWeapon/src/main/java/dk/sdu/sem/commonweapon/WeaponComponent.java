package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Component for entities that can use weapons.
 */
public class WeaponComponent implements IComponent {
	private IWeaponSPI activeWeapon;
	private double lastActivatedTime = 0;

	private final List<IWeaponSPI> weapons;
	private final Map<String, IWeaponSPI> weaponMap = new TreeMap<>();

	private final StatsComponent stats;

	/**
	 * Creates a weapon component.
	 *
	 * @param weapons The weapon implementations
	 */
	public WeaponComponent(StatsComponent stats, List<IWeaponSPI> weapons) {
		this.weapons = weapons;
		this.stats = stats;

		for (IWeaponSPI w : weapons) {
			weaponMap.put(w.getId(), w);
		}

		if (!weapons.isEmpty()) {
			setActiveWeapon(weapons.get(0));
		}
	}

	/**
	 * Updates stats based on the active weapon type
	 */
	private void updateWeaponStats(IWeaponSPI weapon) {
		// Common stats
		stats.setBaseStat(StatType.DAMAGE, weapon.getDamage());
		stats.setBaseStat(StatType.ATTACK_SPEED, weapon.getAttackSpeed());
		stats.setBaseStat(StatType.BULLET_SCALE, weapon.getAttackScale());

		// I still think this is valid
		if (weapon instanceof IRangedWeaponSPI) {
			stats.setBaseStat(StatType.BULLET_SPEED, ((IRangedWeaponSPI) weapon).getBulletSpeed());
		} else {
			stats.setBaseStat(StatType.BULLET_SPEED, 0);
		}
	}

	public IWeaponSPI getActiveWeapon() {
		return activeWeapon;
	}

	/**
	 * Sets the active weapon by reference
	 */
	private void setActiveWeapon(IWeaponSPI weapon) {
		if (weapon == null) return;

		activeWeapon = weapon;
		updateWeaponStats(weapon);
	}

	/**
	 * Sets the active weapon by ID
	 */
	public void setActiveWeapon(String id) {
		IWeaponSPI weapon = weaponMap.get(id);
		if (weapon == null) return;

		setActiveWeapon(weapon);
	}

	public float getDamage() {
		return stats.getStat(StatType.DAMAGE);
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
		return currentTime - lastActivatedTime >= 1 / stats.getStat(StatType.ATTACK_SPEED);
	}

	public List<IWeaponSPI> getWeapons() {
		return weapons;
	}

	public float getBulletSpeed() {
		return stats.getStat(StatType.BULLET_SPEED);
	}

	public float getBulletScale() {
		return stats.getStat(StatType.BULLET_SCALE);
	}

	public float getBulletKnockback() {
		return stats.getStat(StatType.BULLET_KNOCKBACK);
	}

	/**
	 * Checks if the active weapon is a melee weapon
	 */
	public boolean isMeleeWeapon() {
		return activeWeapon instanceof IMeleeWeaponSPI;
	}

	/**
	 * Checks if the active weapon is a ranged weapon
	 */
	public boolean isRangedWeapon() {
		return activeWeapon instanceof IRangedWeaponSPI;
	}

	/**
	 * Gets the active weapon as a melee weapon if it is one
	 */
	public IMeleeWeaponSPI getActiveMeleeWeapon() {
		return activeWeapon instanceof IMeleeWeaponSPI ?
			(IMeleeWeaponSPI) activeWeapon : null;
	}

	/**
	 * Gets the active weapon as a ranged weapon if it is one
	 */
	public IRangedWeaponSPI getActiveRangedWeapon() {
		return activeWeapon instanceof IRangedWeaponSPI ?
			(IRangedWeaponSPI) activeWeapon : null;
	}
}