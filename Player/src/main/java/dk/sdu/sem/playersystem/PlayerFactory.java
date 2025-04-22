package dk.sdu.sem.playersystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.*;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonstats.StatsFactory;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonstats.StatType;

import java.util.ServiceLoader;

public class PlayerFactory implements IPlayerFactory {
	private static final boolean DEBUG = false;

	private static final float COLLIDER_RADIUS = GameConstants.TILE_SIZE * 0.4f;
	private static final float COLLIDER_OFFSET_Y = GameConstants.TILE_SIZE * 0.125f;

	@Override
	public Entity create() {
		return create(new Vector2D(380, 300), 1000.0f, 5.0f);
	}

	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction) {
		if (DEBUG) System.out.println("Creating player entity at position: " + position);

		Entity player = new Entity();

		// Add required components
		player.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		player.addComponent(new PhysicsComponent(friction, 1));
		player.addComponent(new PlayerComponent(moveSpeed));

		// Add stats component
		StatsComponent stats = StatsFactory.createStatsFor(player);
		setupDefaultStats(stats);

		// Add optional weapon component
		ServiceLoader.load(IWeaponSPI.class).findFirst().ifPresent(weapon -> {
			player.addComponent(new WeaponComponent(weapon, 2, 0.5F));
			if (DEBUG) System.out.println("Added weapon component to player");
		});

		// Add weapon
		IWeaponSPI weapon = WeaponRegistry.getWeapon("bullet_weapon");
		if (weapon != null)
			player.addComponent(new WeaponComponent(weapon,2,0.5F));

		// Add inventory component - IMPORTANT for item pickups
		InventoryComponent inventory = new InventoryComponent(30);
		player.addComponent(inventory);

		// Add collider (optional)
		addCollider(player);

		// Add collision listener if collider was added
		if (player.hasComponent(ColliderComponent.class)) {
			player.addComponent(new PlayerCollisionListener());
		}

		// Add sprite renderer with the first frame of idle animation
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_OBJECTS);
		player.addComponent(renderer);
	}

	private void setupAnimator(Entity player) {
		AnimatorComponent animator = new AnimatorComponent();

		animator.addState("idle", "player_idle");
		animator.addState("run", "player_run");
		animator.setCurrentState("idle");

		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		player.addComponent(animator);
	}

	private void addCollider(Entity player) {
		ServiceLoader.load(IColliderFactory.class).findFirst().ifPresent(factory -> {
			Vector2D offset = new Vector2D(0, COLLIDER_OFFSET_Y);

			CircleColliderComponent collider = factory.addCircleCollider(
				player,
				offset,
				COLLIDER_RADIUS,
				PhysicsLayer.PLAYER
			);

			if (collider != null) {
				if (DEBUG) System.out.println("Added collider to player entity");
			} else {
				System.err.println("Failed to add collider to player entity");
			}
		});
	}
}