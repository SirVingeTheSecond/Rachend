package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that handles bullet collision events via triggers.
 * Attached to bullet entities to respond to collision events.
 */
public class BulletTriggerListener implements IComponent, ITriggerListener {
	private final Entity bulletEntity;
	private boolean hitDetected = false;
	private Entity hitEntity = null;

	public BulletTriggerListener(Entity bulletEntity) {
		this.bulletEntity = bulletEntity;
	}

	@Override
	public void onTriggerEnter(Entity other) {
		// Store hit information for processing in the BulletSystem
		hitDetected = true;
		hitEntity = other;

		// Apply damage or other effects based on bullet properties
		BulletComponent bulletComponent = bulletEntity.getComponent(BulletComponent.class);
		if (bulletComponent != null) {
			// Handle bullet hit logic here
			// For example, apply damage to the hit entity
		}
	}

	@Override
	public void onTriggerStay(Entity other) {
		// Only respond to initial collision
	}

	@Override
	public void onTriggerExit(Entity other) {
		// Bullet is passing through, no action needed
	}

	/**
	 * Checks if this bullet has hit something.
	 */
	public boolean isHitDetected() {
		return hitDetected;
	}

	/**
	 * Gets the entity this bullet hit.
	 */
	public Entity getHitEntity() {
		return hitEntity;
	}

	/**
	 * Resets the hit state.
	 */
	public void reset() {
		hitDetected = false;
		hitEntity = null;
	}
}