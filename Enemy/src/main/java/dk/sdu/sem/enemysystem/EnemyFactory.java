package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.ServiceLocator;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatType;

import java.util.ServiceLoader;

public class EnemyFactory implements IEnemyFactory {

	// Enemy collider settings
	private static final float COLLIDER_RADIUS = GameConstants.TILE_SIZE * 0.4f;
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.25f;

	/**
	 * Creates an enemy entity with default settings.
	 */
	@Override
	public Entity create() {
		return create(new Vector2D(500, 400), 200.0f, 5.0f, 50);
	}

	/**
	 * Creates an enemy entity with custom settings.
	 */
	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction, int health) {
		Entity enemy = new Entity();

		// Core components for an enemy
		enemy.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		enemy.addComponent(new PhysicsComponent(friction));

		// Add enemy component with moveSpeed
		EnemyComponent enemyComponent = new EnemyComponent(moveSpeed);
		enemy.addComponent(enemyComponent);

		// Add weapon component
		ServiceLoader<IWeaponSPI> weaponloader = ServiceLoader.load(IWeaponSPI.class);
		IWeaponSPI weapon = weaponloader.iterator().next();
		enemy.addComponent(new WeaponComponent(weapon, 1, 0));

		// Add unified stats component using the factory
		StatsComponent stats = StatsFactory.createStatsFor(enemy);

		// Override default enemy stats if custom values are provided
		stats.setBaseStat(StatType.MAX_HEALTH, health);
		stats.setBaseStat(StatType.CURRENT_HEALTH, health);

		// Customize any specific stats beyond the defaults
		stats.setBaseStat(StatType.DAMAGE, 15f);
		stats.setBaseStat(StatType.ATTACK_RANGE, 35f);

		// Setup sprite renderer
		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("big_demon_idle_anim_f0");
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		enemy.addComponent(renderer);

		// Setup animator component
		AnimatorComponent animator = new AnimatorComponent();

		// Add animation states
		animator.addState("idle", "demon_idle");
		animator.addState("run", "demon_run");

		// Set initial state
		animator.setCurrentState("idle");

		// Add transitions between states
		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		enemy.addComponent(animator);

		// Add a collider for the enemy
		addCollider(enemy);

		return enemy;
	}

	/**
	 * Adds a collider to the enemy entity.
	 */
	private void addCollider(Entity enemy) {
		IColliderFactory factory = ServiceLocator.getColliderFactory();
		if (factory != null) {
			boolean success = factory.addCircleCollider(
				enemy,                // Entity
				0,                    // X offset
				COLLIDER_OFFSET_Y,    // Y offset
				COLLIDER_RADIUS,      // Radius
				PhysicsLayer.ENEMY    // IMPORTANT: Set the correct layer
			);

			if (success) {
				System.out.println("Added collider to enemy entity (layer: ENEMY, radius: " + COLLIDER_RADIUS + ")");
			} else {
				System.out.println("Failed to add collider to enemy entity");
			}
		} else {
			System.out.println("No collision support available for enemy");
		}
	}
}