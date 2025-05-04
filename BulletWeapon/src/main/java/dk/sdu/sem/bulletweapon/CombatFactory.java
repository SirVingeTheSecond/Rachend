package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.BulletComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.PointLightComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for creating combat entities.
 */
public class CombatFactory {
	private static final Logging LOGGER = Logging.createLogger("CombatFactory", LoggingLevel.DEBUG);

	// Configuration
	private static final float DEFAULT_BULLET_RADIUS = 5.0f;
	private static final float DEFAULT_BULLET_SPEED = 100.0f;
	private static final float DEFAULT_BULLET_FRICTION = 0.0f;
	private static final float DEFAULT_BULLET_MASS = 2.0f;

	private final Optional<IColliderFactory> colliderFactory;

	/**
	 * Creates a new combat factory.
	 */
	public CombatFactory() {
		this.colliderFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (colliderFactory.isEmpty()) {
			LOGGER.warn("WARNING: No IColliderFactory implementation found! Combat entities will not have colliders.");
		}
	}

	/**
	 * Creates a bullet entity.
	 *
	 * @param position Starting position
	 * @param direction Direction vector
	 * @param damage Damage amount
	 * @param owner Entity that created this bullet
	 * @return The created bullet entity
	 */
	public Entity createBullet(Vector2D position, Vector2D direction, float damage, float speed, float scale, Entity owner) {
		Entity bullet = new Entity();

		try {
			// Normalize direction
			Vector2D normalizedDirection = direction.normalize();
			float rotation = normalizedDirection.angle();

			// Calculate velocity based on owner's velocity plus bullet direction
			// This maintains the momentum
			Vector2D baseVel = new Vector2D(1, 0).rotate(rotation).scale(speed);
			Vector2D ownerVelocity = new Vector2D(0, 0);

			// Add owner's velocity component if they have physics
			PhysicsComponent ownerPhysics = owner.getComponent(PhysicsComponent.class);
			if (ownerPhysics != null) {
				ownerVelocity = ownerPhysics.getVelocity().scale(0.1f);
			}

			Vector2D velocity = ownerVelocity.add(baseVel);

			// with rotation
			TransformComponent transform = new TransformComponent(position, velocity.angle(), new Vector2D(scale, scale));
			bullet.addComponent(transform);

			BulletComponent projectileComp = new BulletComponent(velocity.magnitude(), damage, owner);
			bullet.addComponent(projectileComp);

			// Add physics component with proper velocity
			PhysicsComponent physics = new PhysicsComponent(0.1f, 1.0f);
			physics.setVelocity(velocity);
			bullet.addComponent(physics);

			// Add animation and sprite renderer components
			try {
				String anim = "fire_bullet_anim";
				if (owner.hasComponent(PlayerComponent.class)) {
					anim = "green_bullet_anim";
				}
				AnimatorComponent animator = new AnimatorComponent(anim);
				bullet.addComponent(animator);

				SpriteRendererComponent renderer = new SpriteRendererComponent();
				renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
				bullet.addComponent(renderer);
			} catch (Exception e) {
				LOGGER.debug("No projectile sprite found, visual representation will be missing");
			}

			if (owner.hasComponent(PlayerComponent.class)) {
				PointLightComponent light = new PointLightComponent(
					64,
					90, 255, 50,
					0.8f,
					true,
					GameConstants.LAYER_EFFECTS
				);
				bullet.addComponent(light);
			} else {
				PointLightComponent light = new PointLightComponent(
					64,
					255,100,0,
					0.8f,
					true,
					GameConstants.LAYER_EFFECTS
				);
				bullet.addComponent(light);
			}

			// Add collider if collision factory is available
			if (colliderFactory.isPresent()) {
				CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
					bullet,
					new Vector2D(0, 0),
					DEFAULT_BULLET_RADIUS * scale,
					true,
					owner.hasComponent(PlayerComponent.class) ? PhysicsLayer.PLAYER_PROJECTILE : PhysicsLayer.ENEMY_PROJECTILE
				);

				if (collider == null) {
					throw new IllegalStateException("Failed to create collider for bullet");
				}
			}

			// Add trigger listener
			bullet.addComponent(new BulletTriggerListener(bullet));

				LOGGER.debug("Created projectile at position %s with direction %s from %s%n",
					position, direction, owner.hasComponent(PlayerComponent.class) ? "player" : "enemy");

			return bullet;

		} catch (Exception e) {
			// Clean up on failure
			if (bullet.getScene() != null) {
				bullet.getScene().removeEntity(bullet);
			}
			throw new RuntimeException("Failed to create projectile: " + e.getMessage(), e);
		}
	}
}