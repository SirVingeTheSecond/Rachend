package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;

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

		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent(
			AssetFacade.getSpriteId("elf_idle", 0));

		spriteRenderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		player.addComponent(spriteRenderer);

		AnimatorComponent animator = new AnimatorComponent();

		// Add animations using string IDs directly
		animator.addAnimation("run", AssetFacade.getAnimationId("elf_run"));
		animator.addAnimation("idle", AssetFacade.getAnimationId("elf_idle"));

		// Set initial state
		animator.playState("idle");

		// Add transitions based on movement
		animator.addTransition("idle", "run",
			parameters -> (boolean) parameters.getOrDefault("isMoving", false));

		animator.addTransition("run", "idle",
			parameters -> !(boolean) parameters.getOrDefault("isMoving", false));

		// Add animator to player
		player.addComponent(animator);

		return player;
	}
}