package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.player.PlayerComponent;

/**
 * Implementation of IWeaponSPI that fires bullet entities.
 */
public class BulletWeapon implements IWeaponSPI {
	private static final float BULLET_RADIUS = 5.0f;
	private static final float BULLET_OFFSET = 20.0f; // Spawn distance from shooter
	private static final boolean DEBUG = false;

	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		WeaponComponent weaponComponent = activator.getComponent(WeaponComponent.class);
		if (weaponComponent == null) return;

		if (Time.getTime() - weaponComponent.lastActivated > weaponComponent.getAttackTimer()) {
			weaponComponent.lastActivated = Time.getTime();

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

			// Add bullet to scene
			SceneManager.getInstance().getActiveScene().addEntity(bullet);

			if (DEBUG) {
				System.out.println("Bullet fired by " +
					(activator.hasComponent(dk.sdu.sem.player.PlayerComponent.class) ? "player" : "enemy") +
					" with damage " + damage);
			}
		}
	}

	private Entity createBullet(Vector2D position, float rotation, Entity owner, int damage) {
		Entity bullet = new Entity();
		AnimatorComponent animator = new AnimatorComponent("fire_bullet_anim");
		bullet.addComponent(animator);
		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent();
		spriteRenderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		bullet.addComponent(spriteRenderer);

		// Add components
		PhysicsComponent physicsComponent = new PhysicsComponent(0.1f, 1f);
		Vector2D baseVel = new Vector2D(1,0).rotate(rotation).scale(200);
		Vector2D velocity = owner.getComponent(PhysicsComponent.class).getVelocity().scale(0.1f).add(baseVel);

		BulletComponent bulletComponent = new BulletComponent((int) velocity.magnitude(), damage, owner);
		bullet.addComponent(bulletComponent);

		bullet.addComponent(new TransformComponent(position, velocity.angle()));
		bullet.addComponent(physicsComponent);

		// Add a trigger handler component
		bullet.addComponent(new BulletTriggerListener(bullet));

		// Add a trigger collider
		// IMPORTANT: isTrigger=true ensures it will work with the trigger system
		// PhysicsLayer.PROJECTILE allows filtering collisions by layer
		bullet.addComponent(new CircleColliderComponent(
			bullet,
			new Vector2D(0, 0),
			BULLET_RADIUS,
			true,
			PhysicsLayer.PROJECTILE
		));

		if (DEBUG) {
			System.out.println("Created bullet at position " + position +
				" with rotation " + rotation + " owned by " +
				(owner.hasComponent(PlayerComponent.class) ? "player" : "enemy"));
		}

		return bullet;
	}
}