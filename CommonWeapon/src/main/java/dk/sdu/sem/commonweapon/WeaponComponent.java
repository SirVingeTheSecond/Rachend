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

		setActiveWeapon(weapons.get(0).getId());
	}

	public IWeaponSPI getActiveWeapon() {
		return activeWeapon;
	}

	public void setActiveWeapon(String id) {
		IWeaponSPI weapon = weaponMap.get(id);
		if (weapon == null)
			return;

		activeWeapon = weapon;
		stats.setBaseStat(StatType.DAMAGE, activeWeapon.getDamage());
		stats.setBaseStat(StatType.BULLET_SPEED, activeWeapon.getBulletSpeed());
		stats.setBaseStat(StatType.ATTACK_SPEED, activeWeapon.getAttackSpeed());
		stats.setBaseStat(StatType.BULLET_SCALE, activeWeapon.getBulletScale());
		stats.setBaseStat(StatType.BULLET_KNOCKBACK, activeWeapon.getBulletKnockback());
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
}