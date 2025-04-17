package dk.sdu.sem.commonweapon;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.events.TriggerEnterEvent;
import dk.sdu.sem.collision.events.TriggerExitEvent;
import dk.sdu.sem.collision.events.TriggerStayEvent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

/**
 * Listener for bullet triggers.
 */
public class BulletTriggerListener implements IComponent, ITriggerListener {
	private final Entity projectileEntity;
	private boolean hitDetected = false;
	private Entity hitEntity = null;

	/**
	 * Creates a bullet trigger listener.
	 *
	 * @param projectileEntity The bullet entity
	 */
	public BulletTriggerListener(Entity projectileEntity) {
		this.projectileEntity = projectileEntity;
	}

	@Override
	public void onTriggerEnter(TriggerEnterEvent event) {
		// Make sure this is for our entity
		if (event.getEntity() != projectileEntity) {
			return;
		}

		// Skip if already hit something
		if (hitDetected) {
			return;
		}

		// Get projectile component
		BulletComponent projectile = projectileEntity.getComponent(BulletComponent.class);
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
		// No action needed for projectiles
	}

	@Override
	public void onTriggerExit(TriggerExitEvent event) {
		// No action needed for projectiles
	}

	/**
	 * Checks if this projectile has hit something.
	 */
	public boolean isHitDetected() {
		return hitDetected;
	}

	/**
	 * Gets the entity this projectile hit.
	 */
	public Entity getHitEntity() {
		return hitEntity;
	}
}