package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.assets.AnimationReference;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;

/**
 * Implementation of IPlayerFactory for creating player entities.
 * Now uses the asset reference system for sprites and animations.
 */
public class PlayerFactory implements IPlayerFactory {

	/**
	 * Creates a player entity with default settings.
	 */
	@Override
	public Entity create() {
		return create(new Vector2D(400, 300), 200.0f, 5.0f);
	}

	/**
	 * Creates a player entity with custom settings.
	 */
	@Override
	public Entity create(Vector2D position, float moveSpeed, float friction) {
		Entity player = new Entity();

		// Add core components
		player.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		player.addComponent(new PhysicsComponent(friction));
		player.addComponent(new PlayerComponent(moveSpeed));

		// Create sprite renderer with null sprite - animation will set it
		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent();
		spriteRenderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		player.addComponent(spriteRenderer);

		// Create animator component with animation states
		AnimatorComponent animator = new AnimatorComponent();

		// Add animations using references
		animator.addAnimation("idle", new AnimationReference("elf_idle_animation"));
		animator.addAnimation("run", new AnimationReference("elf_run_animation"));

		// Set initial state
		animator.playState("idle");

		// Add transition rules
		// When isMoving = true, transition from idle to run
		animator.addTransition("idle", "run",
			parameters -> (boolean) parameters.getOrDefault("isMoving", false));

		// When isMoving = false, transition from run to idle
		animator.addTransition("run", "idle",
			parameters -> !(boolean) parameters.getOrDefault("isMoving", false));

		// Add component to entity
		player.addComponent(animator);

		return player;
	}
}