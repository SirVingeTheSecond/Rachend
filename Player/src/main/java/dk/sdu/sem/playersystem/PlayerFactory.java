package dk.sdu.sem.playersystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.dashability.DashAbilityComponent;
import dk.sdu.sem.gamesystem.Game;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.particlesystem.ParticleEmitterComponent;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatType;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for creating player entities.
 */
public class PlayerFactory implements IPlayerFactory {
	private static final Logging LOGGER = Logging.createLogger("PlayerFactory", LoggingLevel.DEBUG);

	private static final float COLLIDER_RADIUS = GameConstants.TILE_SIZE * 0.4f;
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.125f;

	@Override
	public Entity create() {
		return create(new Vector2D(380, 300), 1000.0f, 5.0f);
	}

	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction) {
		LOGGER.debug("Creating player entity at position: " + position);

		Entity player = new Entity();

		player.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		player.addComponent(new PhysicsComponent(friction, 1));
		player.addComponent(new ParticleEmitterComponent(100));

		PlayerComponent playerComponent = new PlayerComponent();
		player.addComponent(playerComponent);

		DashAbilityComponent dashComponent = new DashAbilityComponent();
		dashComponent.setFadeDelay(0.3);
		dashComponent.setFadeDuration(0.2);
		player.addComponent(dashComponent);

		StatsComponent stats = StatsFactory.createStatsFor(player);

		// Add weapon
		IWeaponSPI weapon = WeaponRegistry.getWeapon("bullet_weapon");
		if (weapon != null)
			player.addComponent(new WeaponComponent(stats, List.of(weapon)));

		// Add inventory component - IMPORTANT for item pickups
		InventoryComponent inventory = new InventoryComponent();
		player.addComponent(inventory);

		// Create a sprite reference for the default idle frame
		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("elf_m_idle_anim_f0");

		// Add sprite renderer with the first frame of idle animation
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		player.addComponent(renderer);

		// Create animator component with states
		AnimatorComponent animator = new AnimatorComponent();

		// Add animation states (using the names created in PlayerAssetProvider)
		animator.addState("idle", "player_idle");
		animator.addState("run", "player_run");
		animator.addState("hurt", "player_hurt");

		// Set initial state
		animator.setCurrentState("idle");

		// Add transitions between states
		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		stats.addStatChangeListener(StatType.CURRENT_HEALTH, (oldValue, newValue) -> {
			if (newValue < oldValue) {
				animator.setOneShotData("hurt", "idle");
				StatModifier invincibilityFrames = StatModifier.createFlat("player_hurt", 100, 0.2f);
				stats.addModifier(StatType.ARMOR, invincibilityFrames);
			}
			if (newValue == 0) {
				player.removeComponent(PhysicsComponent.class);
				Time.after(0.5f, () -> Game.getInstance().gameOver());
			}
		});

		player.addComponent(animator);

		addCollider(player);
		player.addComponent(new PlayerCollisionListener());

		return player;
	}

	/**
	 * Adds a collider to the player entity.
	 */
	private void addCollider(Entity player) {
		// Direct ServiceLoader lookup
		Optional<IColliderFactory> optionalFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (optionalFactory.isPresent()) {
			IColliderFactory factory = optionalFactory.get();

			// Create a Vector2D for the offset
			Vector2D offset = new Vector2D(0, COLLIDER_OFFSET_Y);

			CircleColliderComponent collider = factory.addCircleCollider(
				player,
				offset,
				COLLIDER_RADIUS,
				PhysicsLayer.PLAYER
			);

			if (collider != null) {
				LOGGER.debug("Added collider to player entity (layer: PLAYER, radius: " + COLLIDER_RADIUS + ")");
			} else {
				LOGGER.debug("Failed to add collider to player entity");
			}
		} else {
			LOGGER.debug("No collision support available for player");
		}
	}
}