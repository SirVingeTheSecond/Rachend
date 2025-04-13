package dk.sdu.sem.commonweaponsystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.itemsystem.PickupComponent;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for creating combat-related entities with proper collision components.
 */
public class CombatFactory {
	// Default config
	private static final float DEFAULT_PROJECTILE_SPEED = 800.0f;
	private static final float DEFAULT_PROJECTILE_RADIUS = 5.0f;
	private static final float DEFAULT_PROJECTILE_MASS = 2.0f;
	private static final float DEFAULT_PROJECTILE_FRICTION = 0.0f;

	// Services
	private final Optional<IColliderFactory> colliderFactory;

	/**
	 * Creates a new combat factory and loads required services.
	 *
	 * @throws RuntimeException if critical services cannot be loaded
	 */
	public CombatFactory() {
		this.colliderFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (colliderFactory.isEmpty()) {
			System.err.println("WARNING: No IColliderFactory implementation found! Combat entities will not have colliders.");
		}
	}

	/**
	 * Creates a projectile that deals damage on collision.
	 * Uses composition to ensure the entity has all required components.
	 *
	 * @param position Starting position
	 * @param direction Direction vector (will be normalized)
	 * @param ownerEntity Entity that fired the projectile
	 * @param damage Amount of damage the projectile deals
	 * @return The created projectile entity
	 * @throws RuntimeException if required components cannot be created
	 */
	public Entity createProjectile(Vector2D position, Vector2D direction, Entity ownerEntity, float damage) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create projectile: No IColliderFactory service available");
		}

		Entity projectile = new Entity();

		try {
			// Normalize direction
			Vector2D normalizedDirection = direction.normalize();

			// Step 1: Add core components first - these have no dependencies
			// Add transform component
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1, 1));
			projectile.addComponent(transform);

			// Add physics component with velocity based on direction
			PhysicsComponent physics = new PhysicsComponent(DEFAULT_PROJECTILE_FRICTION, DEFAULT_PROJECTILE_MASS);
			physics.setVelocity(normalizedDirection.scale(DEFAULT_PROJECTILE_SPEED));
			projectile.addComponent(physics);

			// Add stats component with lifetime
			StatsComponent stats = new StatsComponent();
			stats.setBaseStat(StatType.DAMAGE, damage);
			projectile.addComponent(stats);

			// Step 2: Add visual components
			// Try to add a default sprite if available
			try {
				IAssetReference<Sprite> spriteRef = new SpriteReference("projectile");
				SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
				renderer.setRenderLayer(GameConstants.LAYER_EFFECTS);
				projectile.addComponent(renderer);
			} catch (Exception e) {
				System.out.println("No projectile sprite found, visual representation will be missing");
			}

			// Step 3: Add collision components - IColliderFactory must be available
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				projectile,
				new Vector2D(0, 0), // Centered offset
				DEFAULT_PROJECTILE_RADIUS,
				PhysicsLayer.PROJECTILE
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for projectile");
			}

			// Step 4: Add components that depend on other components
			// The DamageComponent requires a ColliderComponent
			DamageComponent damageComponent = new DamageComponent(projectile, damage);
			projectile.addComponent(damageComponent);

			return projectile;

		} catch (Exception e) {
			// Handle errors - clean up any partially created entity
			if (projectile.getScene() != null) {
				projectile.getScene().removeEntity(projectile);
			}
			throw new RuntimeException("Failed to create projectile: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a melee attack hitbox that deals damage on collision.
	 * The hitbox is temporary and will be removed after a specified duration.
	 * Uses composition to ensure the entity has all required components.
	 *
	 * @param position Position relative to attacker
	 * @param radius Radius of the attack
	 * @param ownerEntity Entity performing the attack
	 * @param damage Amount of damage to deal
	 * @param duration Duration in seconds for the attack hitbox to exist
	 * @return The created melee attack entity
	 * @throws RuntimeException if required components cannot be created
	 */
	public Entity createMeleeAttack(Vector2D position, float radius, Entity ownerEntity, float damage, float duration) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create melee attack: No IColliderFactory service available");
		}

		Entity meleeAttack = new Entity();

		try {
			// Step 1: Add core components first - these have no dependencies
			// Add transform component
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1, 1));
			meleeAttack.addComponent(transform);

			// Add stats component with lifetime and damage
			StatsComponent stats = new StatsComponent();
			stats.setBaseStat(StatType.DAMAGE, damage);
			meleeAttack.addComponent(stats);

			// Step 2: Add collision components - must be added before DamageComponent
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				meleeAttack,
				new Vector2D(0, 0), // Centered offset
				radius,
				PhysicsLayer.PROJECTILE // Could use a dedicated ATTACK layer in the future
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for melee attack");
			}

			// Make it a trigger so it overlaps without pushing
			collider.setTrigger(true);

			// Step 3: Add components that depend on other components
			// DamageComponent requires a ColliderComponent
			DamageComponent damageComponent = new DamageComponent(meleeAttack, damage);
			meleeAttack.addComponent(damageComponent);

			return meleeAttack;

		} catch (Exception e) {
			// Handle errors - clean up any partially created entity
			if (meleeAttack.getScene() != null) {
				meleeAttack.getScene().removeEntity(meleeAttack);
			}
			throw new RuntimeException("Failed to create melee attack: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a pickup item entity with all required components.
	 * Uses composition to ensure the entity has all required components.
	 *
	 * @param position Item position
	 * @param itemType Type of item (e.g., "health", "coin", "weapon")
	 * @param value Value/amount of the item
	 * @param radius Collision radius for the pickup
	 * @param spriteName Name of the sprite to use for visual representation
	 * @return The created pickup entity
	 * @throws RuntimeException if required components cannot be created
	 */
	public Entity createPickup(Vector2D position, String itemType, float value, float radius, String spriteName) {
		if (colliderFactory.isEmpty()) {
			throw new IllegalStateException("Cannot create pickup: No IColliderFactory service available");
		}

		Entity pickup = new Entity();

		try {
			// Step 1: Add core components
			// Add transform component
			TransformComponent transform = new TransformComponent(position, 0, new Vector2D(1, 1));
			pickup.addComponent(transform);

			// Step 2: Add visual components if sprite name is provided
			if (spriteName != null && !spriteName.isEmpty()) {
				try {
					IAssetReference<Sprite> spriteRef = new SpriteReference(spriteName);
					SpriteRendererComponent renderer = new SpriteRendererComponent(spriteRef);
					renderer.setRenderLayer(GameConstants.LAYER_MIDGROUND);
					pickup.addComponent(renderer);
				} catch (Exception e) {
					System.out.println("Sprite '" + spriteName + "' not found, visual representation will be missing");
				}
			}

			// Step 3: Add collision components - must be added before PickupComponent
			CircleColliderComponent collider = colliderFactory.get().addCircleCollider(
				pickup,
				new Vector2D(0, 0), // Centered offset
				radius,
				PhysicsLayer.ITEM
			);

			if (collider == null) {
				throw new IllegalStateException("Failed to create collider for pickup");
			}

			// Make it a trigger so it doesn't block movement
			collider.setTrigger(true);

			// Step 4: Add item-specific components
			// Create pickup component that handles item collection
			// ToDo: Ensure this does not create dependency on Item module.
			PickupComponent pickupComponent = new PickupComponent(pickup, itemType, value);
			pickup.addComponent(pickupComponent);

			return pickup;

		} catch (Exception e) {
			// Handle errors - clean up any partially created entity
			if (pickup.getScene() != null) {
				pickup.getScene().removeEntity(pickup);
			}
			throw new RuntimeException("Failed to create pickup: " + e.getMessage(), e);
		}
	}
}