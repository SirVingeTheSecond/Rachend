package dk.sdu.sem.commonweaponsystem;
import dk.sdu.sem.commonsystem.Entity;

public interface IWeapon {
	/*
	 * Interface for activating weapon nodes, activator is a reference to the
	 *  entity which activated the Weapon.
	 */
	public void activateWeapon(Entity activator);

}
