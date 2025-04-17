package dk.sdu.sem.commonweapon;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Service Provider Interface for weapons.
 */
public interface IWeaponSPI {
	/**
	 * Activates the weapon with specified direction
	 *
	 * @param activator Entity activating the weapon
	 * @param direction Direction vector for the weapon
	 */
	void activateWeapon(Entity activator, Vector2D direction);
}