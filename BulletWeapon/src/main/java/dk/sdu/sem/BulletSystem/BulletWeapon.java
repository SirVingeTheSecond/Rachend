package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;

/**
 * Implementation of IWeaponSPI that fires bullet entities.
 */
public class BulletWeapon implements IWeaponSPI {
	private static final float BULLET_RADIUS = 5.0f;
	private static final float BULLET_OFFSET = 20.0f; // Spawn distance from shooter

	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		WeaponComponent weaponComponent = activator.getComponent(WeaponComponent.class);
		if (weaponComponent == null) return;

		if (Time.getTime() > weaponComponent.lastActivated) {
			weaponComponent.lastActivated =
				Time.getTime() + 7 - weaponComponent.getAttackTimer();

			TransformComponent activatorTransform =
				activator.getComponent(TransformComponent.class);

			if (activatorTransform == null) return;

			// Calculate spawn position (offset from shooter in the direction of fire)
			Vector2D spawnPosition = activatorTransform.getPosition()
				.add(direction.normalize().scale(BULLET_OFFSET));

			// Get damage from the weapon component
			int damage = weaponComponent.getDamage();

			// Create bullet with damage and owner information
			Entity bullet = createBullet(
				spawnPosition,
				direction.normalize().angle(),
				activator,
				damage
			);

			SceneManager.getInstance().getActiveScene().addEntity(bullet);
		}
	}

	private Entity createBullet(Vector2D position, float rotation, Entity owner, int damage) {
		Entity bullet = new Entity();

		// Add components
		bullet.addComponent(new BulletComponent(40, damage, owner));
		bullet.addComponent(new TransformComponent(position, rotation));
		bullet.addComponent(new PhysicsComponent(0.1f, 1.0f));

		// Add a trigger handler component
		bullet.addComponent(new BulletTriggerHandler(bullet));

		// Add a trigger collider
		bullet.addComponent(new dk.sdu.sem.collision.components.ColliderComponent(
			bullet,
			new Vector2D(0, 0),
			BULLET_RADIUS,
			true,
			PhysicsLayer.PROJECTILE
		));

		return bullet;
	}
}