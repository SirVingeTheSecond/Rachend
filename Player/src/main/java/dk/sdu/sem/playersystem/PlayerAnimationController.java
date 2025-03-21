package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.ResourceManager;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * System that updates player animations based on actions.
 */
public class PlayerAnimationController implements IUpdate {
	// Map to store animations by player entity ID
	private final Map<String, PlayerAnimations> playerAnimationsMap = new HashMap<>();

	@Override
	public void update() {
		// Get all player nodes
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		for (PlayerNode node : playerNodes) {
			// First we need to ensure the player has animations
			ensurePlayerHasAnimations(node);

			SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);
			PhysicsComponent physics = node.physicsComponent;

			if (renderer == null) continue;

			// Get animation set for this player
			PlayerAnimations animations = playerAnimationsMap.get(node.getEntity().getID());

			// Skip if we failed to create animations
			if (animations == null) {
				System.err.println("No animations for player: " + node.getEntity().getID());
				continue;
			}

			// Determine animation state based on velocity
			Vector2D velocity = physics.getVelocity();
			boolean isMoving = velocity.magnitudeSquared() > 0.5f; // Small threshold to avoid flicker

			// Flip sprite based on horizontal movement direction
			if (velocity.getX() < -0.1f) {
				renderer.setFlipX(true);
			} else if (velocity.getX() > 0.1f) {
				renderer.setFlipX(false);
			}

			// Set animation
			if (isMoving) {
				if (renderer.getCurrentAnimation() != animations.runAnimation) {
					renderer.setCurrentAnimation(animations.runAnimation);
				}
			} else {
				if (renderer.getCurrentAnimation() != animations.idleAnimation) {
					renderer.setCurrentAnimation(animations.idleAnimation);
				}
			}
		}
	}

	private void ensurePlayerHasAnimations(PlayerNode node) {
		String entityId = node.getEntity().getID();

		// If we already have animations for this player, skip
		if (playerAnimationsMap.containsKey(entityId)) {
			return;
		}

		// Create animations for this player
		PlayerAnimations animations = createPlayerAnimations();
		if (animations != null) {
			playerAnimationsMap.put(entityId, animations);

			// Set initial animation
			SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);
			if (renderer != null) {
				renderer.setCurrentAnimation(animations.idleAnimation);
			}
		}
	}

	private PlayerAnimations createPlayerAnimations() {
		ResourceManager resourceManager = ResourceManager.getInstance();

		// Create run animation
		List<Sprite> runFrames = new ArrayList<>();
		boolean loadSuccess = true;

		for (int i = 0; i < 4; i++) {
			String framePath = "elf_m_run_anim_f" + i + ".png";
			Sprite frame = new Sprite("elf_run_" + i, resourceManager.loadImage(framePath));

			// Check if image was loaded
			if (frame.getImage() == null) {
				System.err.println("Failed to load player animation frame: " + framePath);
				loadSuccess = false;
				break;
			}

			runFrames.add(frame);
		}

		if (!loadSuccess || runFrames.isEmpty()) {
			System.err.println("Failed to create player animations");
			return null;
		}

		SpriteAnimation runAnimation = new SpriteAnimation(runFrames, GameConstants.DEFAULT_FRAME_DURATION, true);

		// For now, I just use the first frame for idle
		List<Sprite> idleFrames = new ArrayList<>();
		idleFrames.add(runFrames.get(0));
		SpriteAnimation idleAnimation = new SpriteAnimation(idleFrames, GameConstants.DEFAULT_FRAME_DURATION, true);

		System.out.println("Successfully created player animations");
		return new PlayerAnimations(runAnimation, idleAnimation);
	}

	/**
	 * Holds the different animations for the player
	 */
	private record PlayerAnimations(
		SpriteAnimation runAnimation,
		SpriteAnimation idleAnimation) {
	}
}