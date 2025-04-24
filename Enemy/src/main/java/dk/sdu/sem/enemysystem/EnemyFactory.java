package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.pathfindingsystem.PathfindingComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Optional;
import java.util.ServiceLoader;

public class EnemyFactory implements IEnemyFactory {
	private static Logging LOGGER = Logging.createLogger("EnemyFactory", LoggingLevel.DEBUG);

	private static final float COLLIDER_RADIUS = GameConstants.TILE_SIZE * 0.4f;
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.125f;

	/**
	 * Creates an enemy entity with default settings.
	 */
	@Override
	public Entity create() {
		return create(new Vector2D(500, 400), 300, 5.0f, 1);
	}

	/**
	 * Creates an enemy entity with custom settings.
	 */
	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction, int health) {
		LOGGER.debug("Creating enemy with " + health + " health at position " + position);

		Entity enemy = new Entity();

		// Core components for an enemy
		enemy.addComponent(new TransformComponent(position, 0, new Vector2D(2,2)));
		enemy.addComponent(new PhysicsComponent(friction, 0.5f));
		enemy.addComponent(new EnemyComponent(moveSpeed));
		enemy.addComponent(new PathfindingComponent(() -> {
			// TODO: optimize (scene entity traversal per half second per enemy)
			TransformComponent playerTransform = Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class)
					.stream()
					.findFirst()
					.map(entity -> entity.getComponent(TransformComponent.class))
					.orElse(null);

		// Add weapon component
		IWeaponSPI weapon = WeaponRegistry.getWeapon("bullet_weapon");
		if (weapon != null)
			enemy.addComponent(new WeaponComponent(weapon, 1, 1));

		// Add unified stats component using the factory
		StatsComponent stats = StatsFactory.createStatsFor(enemy);

		// Set enemy health to exactly 1 HP for one-shot kills
		stats.setBaseStat(StatType.MAX_HEALTH, 1f);
		stats.setBaseStat(StatType.CURRENT_HEALTH, 1f);

		// Set other stats
		stats.setBaseStat(StatType.DAMAGE, 15f);
		stats.setBaseStat(StatType.ATTACK_RANGE, 35f);

		LOGGER.debug("Enemy stats initialized: Health=" +
			stats.getCurrentHealth() + "/" + stats.getMaxHealth() +
			", Damage=" + stats.getBaseStat(StatType.DAMAGE));


		// Setup sprite renderer
			return Optional.ofNullable(playerTransform)
					.map(TransformComponent::getPosition);
		}));

		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("big_demon_idle_anim_f0");
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
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
		enemy.addComponent(new EnemyCollisionListener());

		return enemy;
	}

	/**
	 * Adds a collider to the enemy entity.
	 */
	private void addCollider(Entity enemy) {
		Optional<IColliderFactory> optionalFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (optionalFactory.isPresent()) {
			IColliderFactory factory = optionalFactory.get();

			Vector2D offset = new Vector2D(0, COLLIDER_OFFSET_Y);

			CircleColliderComponent collider = factory.addCircleCollider(
				enemy,
				offset,
				COLLIDER_RADIUS,
				PhysicsLayer.ENEMY
			);

			if (collider != null) {
				LOGGER.debug("Added collider to enemy entity (layer: ENEMY, radius: " + COLLIDER_RADIUS + ")");
			} else {
				LOGGER.error("Failed to add collider to enemy entity");
			}
		} else {
			LOGGER.debug("No collision support available for enemy");
		}
	}
}