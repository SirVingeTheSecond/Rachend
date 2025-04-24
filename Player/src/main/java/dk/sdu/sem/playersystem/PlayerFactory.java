package dk.sdu.sem.playersystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for creating player entities.
 */
public class PlayerFactory implements IPlayerFactory {
	private static final Logging LOGGER = Logging.createLogger("PlayerFactory", LoggingLevel.DEBUG);

	private static final float COLLIDER_RADIUS = GameConstants.TILE_SIZE * 0.4f;
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.125f;

	public IWeaponSPI weapon;

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

		// Movement speed should be a part of stats component
		PlayerComponent playerComponent = new PlayerComponent(moveSpeed);
		player.addComponent(playerComponent);

		StatsComponent stats = StatsFactory.createStatsFor(player);

		stats.setBaseStat(StatType.MAX_HEALTH, 3);
		stats.setBaseStat(StatType.CURRENT_HEALTH, 3);
		stats.setBaseStat(StatType.DAMAGE, 25f);

		// Add weapon
		IWeaponSPI weapon = WeaponRegistry.getWeapon("melee_sweep");
		if (weapon != null)
			player.addComponent(new WeaponComponent(weapon,2,0.5F));

		// Add inventory component - IMPORTANT for item pickups
		InventoryComponent inventory = new InventoryComponent(30);
		player.addComponent(inventory);

		LOGGER.debug("Player created with inventory component (capacity: " + inventory.getMaxCapacity() + ")");

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

		// Set initial state
		animator.setCurrentState("idle");

		// Add transitions between states
		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

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