package dk.sdu.sem.playersystem;

//import dk.sdu.sem.BulletSystem.BulletWeapon;
import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.ServiceLocator;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.weaponsystem.WeaponComponent;
import dk.sdu.sem.commonInventory.PassiveItemInventory;
import dk.sdu.sem.commonInventory.ActiveItemInventory;
import dk.sdu.sem.commoninventory.InventoryComponent;

import java.util.ServiceLoader;

/**
 * Factory for creating player entities with correctly positioned colliders.
 * Uses the reference-based approach for sprites and animations.
 */
public class PlayerFactory implements IPlayerFactory {
	private static final boolean DEBUG = false;

	// Offset for the collider to match the visual representation
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.25f;
	public IWeaponSPI weapon;
	@Override
	public Entity create() {
		return create(new Vector2D(400, 300), 1000.0f, 5.0f);
	}

	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction) {
		if (DEBUG) System.out.println("Creating player entity at position: " + position);

		Entity player = new Entity();

		// Add core components
		player.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		player.addComponent(new PhysicsComponent(friction));
		player.addComponent(new PlayerComponent(moveSpeed));
		player.addComponent(new HealthComponent(3, 3));
		player.addComponent(new PassiveItemInventory());
		player.addComponent(new ActiveItemInventory());
		ServiceLoader<IWeaponSPI> weaponloader = ServiceLoader.load(IWeaponSPI.class);
		weapon = weaponloader.iterator().next();


		player.addComponent(new WeaponComponent(weapon,2,3.5F));

		// Add inventory component - IMPORTANT for item pickups
		InventoryComponent inventory = new InventoryComponent(30);
		player.addComponent(inventory);

		System.out.println("Player created with inventory component (capacity: " + inventory.getMaxCapacity() + ")");

		// Create a sprite reference for the default idle frame
		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("elf_m_idle_anim_f0");

		// Add sprite renderer with the first frame of idle animation
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
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

		// Add a collider with Y offset to match player sprite center
		// IMPORTANT: Set the proper physics layer for collision filtering
		float colliderRadius = GameConstants.TILE_SIZE * 0.35f;
		addColliderWithOffset(player, colliderRadius);

		return player;
	}

	/**
	 * Adds a collider with appropriate Y offset to match the visual representation.
	 *
	 * @param player The player entity
	 * @param colliderRadius The radius of the collider
	 */
	private void addColliderWithOffset(Entity player, float colliderRadius) {
		IColliderFactory factory = ServiceLocator.getColliderFactory();
		if (factory != null) {
			// Add collider with offset to match the character's center mass
			// IMPORTANT: Set PhysicsLayer.PLAYER for proper collision filtering
			boolean success = factory.addCircleCollider(
				player,               // Entity
				0,                    // X offset
				COLLIDER_OFFSET_Y,    // Y offset
				colliderRadius,       // Radius
				PhysicsLayer.PLAYER   // IMPORTANT: Set the correct layer
			);

			if (success) {
				System.out.println("Added collider to player entity (layer: PLAYER, radius: " + colliderRadius + ")");
			} else {
				System.out.println("Failed to add collider to player entity");
			}
		} else {
			System.out.println("No collision support available for player");
		}
	}

	@Override
	public void addColliderIfAvailable(Entity player, float colliderRadius) {
		addColliderWithOffset(player, colliderRadius);
	}
}