package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.components.ColliderComponent;
//import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

public class MeleeHitTriggerListener implements ITriggerListener, IComponent {
// list of weapons

	// for handleing when something hits the hitbox of the melee weapon.
	@Override
	public void onTriggerEnter(Entity other) {
		// first check other entity has health at all else igonore it
//		if (other.hasComponent(HealthComponent.class)){
			// how do I get the damage from the damage doer, not the one which takes damage ?

			// normally this would be Weaponcomponent.damage

			// some way to get the collisionpair for other's other, such that I can get the entity which activated the weapon

			// nodemanager get the Collision node ?
//		}

	}

	@Override
	public void onTriggerStay(Entity other) {

	}

	@Override
	public void onTriggerExit(Entity other) {
		// apply damage/effects to hit entity

	}
}
