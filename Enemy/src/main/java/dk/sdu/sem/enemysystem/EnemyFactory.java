package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.pathfindingsystem.PathfindingComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.ServiceLoader;

public class EnemyFactory implements IEnemyFactory {
	private static final boolean DEBUG = true;

	private static final float COLLIDER_RADIUS = GameConstants.TILE_SIZE * 0.4f;
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.125f;

	@Override
	public Entity create() {
		return create(new Vector2D(500, 400), 300, 5.0f, 1);
	}

	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction, int health) {
		if (DEBUG) System.out.println("Creating enemy with " + health + " health at position " + position);

		Entity enemy = new Entity();

		// Add required core components
		enemy.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
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

		// Add stats component
		StatsComponent stats = StatsFactory.createStatsFor(enemy);
		setupStats(stats);

		// Add sprite renderer
		setupSpriteRenderer(enemy);

		// Add animator
		setupAnimator(enemy);

		// Add collider (optional)
		addCollider(enemy);

		// Add collision listener if collider was added
		if (enemy.hasComponent(ColliderComponent.class)) {
			enemy.addComponent(new EnemyCollisionListener());
		}

		return enemy;
	}

	private void setupStats(StatsComponent stats) {
		// Set enemy health to exactly 1 HP for one-shot kills
		stats.setBaseStat(StatType.MAX_HEALTH, 1f);
		stats.setBaseStat(StatType.CURRENT_HEALTH, 1f);

		// Set other stats
		stats.setBaseStat(StatType.DAMAGE, 15f);
		stats.setBaseStat(StatType.ATTACK_RANGE, 35f);
	}

		if (DEBUG) {
			System.out.println("Enemy stats initialized: Health=" +
				stats.getCurrentHealth() + "/" + stats.getMaxHealth() +
				", Damage=" + stats.getBaseStat(StatType.DAMAGE));
		}

		// Setup sprite renderer
			return Optional.ofNullable(playerTransform)
					.map(TransformComponent::getPosition);
		}));

		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("big_demon_idle_anim_f0");
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		enemy.addComponent(renderer);
	}

	private void setupAnimator(Entity enemy) {
		AnimatorComponent animator = new AnimatorComponent();

		animator.addState("idle", "demon_idle");
		animator.addState("run", "demon_run");
		animator.setCurrentState("idle");

		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		enemy.addComponent(animator);
	}

	private void addCollider(Entity enemy) {
		ServiceLoader.load(IColliderFactory.class).findFirst().ifPresent(factory -> {
			Vector2D offset = new Vector2D(0, COLLIDER_OFFSET_Y);

			CircleColliderComponent collider = factory.addCircleCollider(
				enemy,
				offset,
				COLLIDER_RADIUS,
				PhysicsLayer.ENEMY
			);

			if (collider != null) {
				if (DEBUG) System.out.println("Added collider to enemy entity");
			} else {
				System.err.println("Failed to add collider to enemy entity");
			}
		});
	}
}