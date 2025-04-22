package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.events.TriggerEnterEvent;
import dk.sdu.sem.collision.events.TriggerExitEvent;
import dk.sdu.sem.collision.events.TriggerStayEvent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonweapon.BulletComponent;
import dk.sdu.sem.commonweapon.DamageUtils;

/**
 * Listener for bullet triggers.
 */
public class BulletTriggerListener implements IComponent, ITriggerListener {
	private final Entity bulletEntity;
	private boolean hitDetected = false;
	private Entity hitEntity = null;

	/**
	 * Creates a bullet trigger listener.
	 *
	 * @param bulletEntity The bullet entity
	 */
	public BulletTriggerListener(Entity bulletEntity) {
		this.bulletEntity = bulletEntity;
	}

	@Override
	public void onTriggerEnter(TriggerEnterEvent event) {
		// Make sure this is for our entity
		if (event.getEntity() != bulletEntity) {
			return;
		}

		// Skip if already hit something
		if (hitDetected) {
			return;
		}

		// Get bullet component
		BulletComponent projectile = bulletEntity.getComponent(BulletComponent.class);
		if (projectile == null) {
			return;
		}

		// Skip if owner is hit
		Entity other = event.getOther();
		if (other == projectile.getOwner()) {
			return;
		}

		// Record hit
		hitDetected = true;
		hitEntity = other;
		projectile.setDamaged(true);

		// Apply damage
		DamageUtils.applyDamage(other, projectile.getDamage());
	}

	@Override
	public void onTriggerStay(TriggerStayEvent event) {
		// No action needed for bullets
	}

	@Override
	public void onTriggerExit(TriggerExitEvent event) {
		// No action needed for bullets
	}

	/**
	 * Checks if this bullets has hit something.
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
}