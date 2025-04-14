package dk.sdu.sem.commonweaponsystem;

import dk.sdu.sem.collision.data.Collision;
import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonstats.StatsComponent;

/**
 * Component that deals damage to entities on collision.
 * Uses the collision system's events to detect collisions.
 *
 * REQUIRES: The entity must have a ColliderComponent to receive collision events.
 */
public class DamageComponent implements IComponent, ICollisionListener {
	private final float damageAmount;
	private final Entity owner;
	private boolean hasDamaged = false;
	private static final boolean DEBUG = true;

	/**
	 * Creates a damage component that deals damage on collision.
	 *
	 * @param owner The entity this component is attached to
	 * @param damageAmount Amount of damage to deal
	 * @throws IllegalStateException if the owner entity doesn't have a required component
	 */
	public DamageComponent(Entity owner, float damageAmount) {
		this.owner = owner;
		this.damageAmount = damageAmount;

		// Validate that required components exist
		validateRequiredComponents();
	}

	/**
	 * Validates that all required components exist on the owner entity.
	 *
	 * @throws IllegalStateException if a required component is missing
	 */
	private void validateRequiredComponents() {
		if (owner == null) {
			throw new IllegalArgumentException("Owner entity cannot be null");
		}

		if (!owner.hasComponent(ColliderComponent.class)) {
			throw new IllegalStateException("DamageComponent requires a ColliderComponent on the owner entity to function");
		}
	}

	@Override
	public void onCollisionEnter(Collision collision) {
		if (hasDamaged) return; // Only deal damage once

		Entity other = collision.getEntity();
		if (DEBUG) System.out.println("Collision detected between " + owner.getID() + " and " + other.getID());

		// Get the stats component if it exists
		StatsComponent targetStats = other.getComponent(StatsComponent.class);
		if (targetStats == null) return;

		// Deal damage by reducing current health
		float currentHealth = targetStats.getCurrentHealth();
		targetStats.setCurrentHealth(currentHealth - damageAmount);

		if (DEBUG) {
			System.out.println("Dealt " + damageAmount + " damage to " + other.getID());
			System.out.println("Health reduced from " + currentHealth + " to " + targetStats.getCurrentHealth());
		}

		// Mark as having dealt damage
		hasDamaged = true;
	}

	@Override
	public void onCollisionStay(Collision collision) {
		// For this component, we only deal damage on initial collision
	}

	@Override
	public void onCollisionExit(Collision collision) {
		// Reset damage flag when collision ends
		hasDamaged = false;
	}

	/**
	 * Gets the damage amount.
	 */
	public float getDamageAmount() {
		return damageAmount;
	}

	/**
	 * Checks if this component has already dealt damage in the current collision.
	 */
	public boolean hasDamaged() {
		return hasDamaged;
	}

	/**
	 * Resets the damage flag, allowing the component to deal damage again.
	 */
	public void resetDamage() {
		hasDamaged = false;
	}
}