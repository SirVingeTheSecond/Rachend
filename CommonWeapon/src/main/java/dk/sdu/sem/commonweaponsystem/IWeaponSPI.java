package dk.sdu.sem.commonweaponsystem;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

public interface IWeaponSPI {
	/*
	 * Interface for activating weapon nodes, activator is a reference to the
	 *  entity which activated the Weapon.
	 * direction is an optional argument which can be used for additional
	 * behavior for which direction the weapon targets.
	 */
	void activateWeapon(Entity activator, Vector2D direction);

}
