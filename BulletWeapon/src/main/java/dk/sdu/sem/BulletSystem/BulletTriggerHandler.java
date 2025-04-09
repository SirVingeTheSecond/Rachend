package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.player.PlayerComponent;

/**
 * Handles bullet trigger collisions with other entities.
 * Implements ITriggerListener to receive collision events.
 */
public class BulletTriggerHandler implements IComponent, ITriggerListener {
	private final Entity bulletEntity;

	public BulletTriggerHandler(Entity bulletEntity) {
		this.bulletEntity = bulletEntity;
	}

	@Override
	public void onTriggerEnter(Entity other) {
		BulletComponent bullet = bulletEntity.getComponent(BulletComponent.class);
		if (bullet == null) return;

		// Skip if hitting its owner
		if (bullet.getOwner() == other) return;

		// Handle damage if target has stats component
		if (other.getComponent(StatsComponent.class) != null) {
			handleDamage(other, bullet);
		}

		// Always remove bullet after hitting anything
		if (bulletEntity.getScene() != null) {
			bulletEntity.getScene().removeEntity(bulletEntity);
		}
	}

	private void handleDamage(Entity target, BulletComponent bullet) {
		// Get stats component if available
		StatsComponent stats = target.getComponent(StatsComponent.class);
		if (stats == null) return;

		// Check if the bullet is from player and target is enemy
		boolean isPlayerBullet = bullet.getOwner() != null &&
			bullet.getOwner().hasComponent(PlayerComponent.class);
		boolean isEnemyTarget = target.hasComponent(EnemyComponent.class);

		// Check if the bullet is from enemy and target is player
		boolean isEnemyBullet = bullet.getOwner() != null &&
			bullet.getOwner().hasComponent(EnemyComponent.class);
		boolean isPlayerTarget = target.hasComponent(PlayerComponent.class);

		// Apply damage if valid hit
		if ((isPlayerBullet && isEnemyTarget) || (isEnemyBullet && isPlayerTarget)) {
			// Apply damage to stats
			float currentHealth = stats.getCurrentHealth();
			float newHealth = currentHealth - bullet.getDamage();
			stats.setCurrentHealth(newHealth);

			System.out.println("Hit " + (isEnemyTarget ? "enemy" : "player") +
				" for " + bullet.getDamage() + " damage! Health: " +
				currentHealth + " -> " + newHealth);

			// Check if entity died
			if (newHealth <= 0) {
				handleEntityDeath(target);
			}
		}
	}

	private void handleEntityDeath(Entity target) {
		// Remove the entity from the scene
		if (target.getScene() != null) {
			System.out.println("Entity killed: " + target.getID());
			target.getScene().removeEntity(target);
		}
	}

	@Override
	public void onTriggerStay(Entity other) {
		// Not needed for bullets (one-time hit)
	}

	@Override
	public void onTriggerExit(Entity other) {
		// Not needed for bullets
	}
}