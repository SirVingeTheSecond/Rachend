package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.ResourceManager;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.player.IPlayerFactory;
import dk.sdu.sem.player.PlayerComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of IPlayerFactory for creating player entities.
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
		player.addComponent(new TransformComponent(position, 0, new Vector2D(2, 2)));
		player.addComponent(new PhysicsComponent(friction));
		player.addComponent(new PlayerComponent(moveSpeed));

		SpriteRendererComponent spriteRenderer = new SpriteRendererComponent();
		spriteRenderer.setRenderLayer(GameConstants.LAYER_CHARACTERS);
		player.addComponent(spriteRenderer);

		SpriteAnimation runAnimation = createPlayerRunAnimation();
		if (runAnimation != null) {
			spriteRenderer.setCurrentAnimation(runAnimation);
			System.out.println("Player created with animation");
		} else {
			System.err.println("Failed to create player animation");
		}

		return player;
	}

	/**
	 * Creates a run animation for the player.
	 */
	private SpriteAnimation createPlayerRunAnimation() {
		ResourceManager resourceManager = ResourceManager.getInstance();

		// Load the individual frames
		List<Sprite> frames = new ArrayList<>();
		boolean loadSuccess = true;

		for (int i = 0; i < 4; i++) {
			String framePath = "elf_m_run_anim_f" + i + ".png";
			var image = resourceManager.loadImage(framePath);

			if (image == null) {
				System.err.println("Failed to load player animation frame: " + framePath);
				loadSuccess = false;
				break;
			}

			Sprite frame = new Sprite("elf_run_" + i, image);
			frames.add(frame);
		}

		if (!loadSuccess || frames.isEmpty()) {
			return null;
		}

		// true = looping
		return new SpriteAnimation(frames, GameConstants.DEFAULT_FRAME_DURATION, true);
	}
}