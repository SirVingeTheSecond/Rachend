package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;

public class MeleeHitTriggerListener implements ITriggerListener, IComponent {
	Entity weaponActivator;
	public MeleeHitTriggerListener(Entity weaponActivator) {
		this.weaponActivator = weaponActivator;
	}
// list of weapons

	// for handleing when something hits the hitbox of the melee weapon.
	@Override
	public void onTriggerEnter(Entity other) {
		// first check other entity has health at all else igonore it
		if (other.hasComponent(HealthComponent.class)){
			other.getComponent(HealthComponent.class).subHealth(weaponActivator.getComponent(WeaponComponent.class).getDamage());
		}




	}

	@Override
	public void onTriggerStay(Entity other) {

	}

	@Override
	public void onTriggerExit(Entity other) {
		// apply damage/effects to hit entity

	}
}
