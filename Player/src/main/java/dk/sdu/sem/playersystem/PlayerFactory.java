package dk.sdu.sem.playersystem;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.ServiceLocator;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;

/**
 * Factory for creating player entities.
 */
public class PlayerFactory implements IPlayerFactory {

	@Override
	public Entity create() {
		return create(new Vector2D(400, 300), 1000.0f, 5.0f);
	}

	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction) {
		Entity player = new Entity();

		// Add core components
		player.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		player.addComponent(new PhysicsComponent(friction));
		player.addComponent(new PlayerComponent(moveSpeed));

		// Add sprite renderer with the first frame of idle animation
		SpriteRendererComponent renderer = new SpriteRendererComponent("elf_m_idle_anim_f0");
		renderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		player.addComponent(renderer);

		// Create animator component with states
		AnimatorComponent animator = new AnimatorComponent();

		// Add animation states (remember to use the names from the given provider!)
		animator.addState("idle", "player_idle");
		animator.addState("run", "player_run");

		// Set initial state
		animator.playState("idle");

		// Add transitions between states
		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		player.addComponent(animator);

		// Try to add a collider if the Collision module is available
		float colliderRadius = GameConstants.TILE_SIZE * 0.4f; // Smaller than the tile size
		addColliderIfAvailable(player, colliderRadius);

		return player;
	}

	@Override
	public void addColliderIfAvailable(Entity player, float colliderRadius) {
		IColliderFactory factory = ServiceLocator.getColliderFactory();
		if (factory != null) {
			if (factory.addCircleCollider(player, 0, 0, colliderRadius)) {
				System.out.println("Added collider to player entity");
			}
		} else {
			System.out.println("No collision support available for player");
		}
	}
}