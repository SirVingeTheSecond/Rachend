package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;
import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.weaponsystem.WeaponComponent;

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
		player.addComponent(new HealthComponent(3, 3));

		Entity bullet = new Entity();
		bullet.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		bullet.addComponent(new PhysicsComponent(0));
		bullet.addComponent(new SpriteRendererComponent("Bullet.png"));

		player.addComponent(new WeaponComponent(bullet));

		// Add sprite renderer with the first frame of idle animation
		SpriteRendererComponent renderer = new SpriteRendererComponent("elf_m_idle_anim_f0");
		renderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		player.addComponent(renderer);

		// Create animator component with states
		AnimatorComponent animator = new AnimatorComponent();

		// Add animation states using the names defined in PlayerAssetProvider
		animator.addState("idle", "player_idle");
		animator.addState("run", "player_run");

		// Set initial state
		animator.playState("idle");

		// Add transitions between states
		animator.addTransition("idle", "run", "isMoving", true);
		animator.addTransition("run", "idle", "isMoving", false);

		player.addComponent(animator);

		return player;
	}
}