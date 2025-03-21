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
 * System that updates player animations based on movement.
 */
// AnimationController could be made its own class
public class PlayerAnimationController implements IUpdate {
	// Map to store animations by player entity ID
	private final Map<String, PlayerAnimations> playerAnimationsMap = new HashMap<>();

	@Override
	public void update() {
		// Get all player nodes
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		for (PlayerNode node : playerNodes) {
			// Ensure this player has animations
			ensurePlayerHasAnimations(node);

			// Get relevant components
			SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);
			PhysicsComponent physics = node.physicsComponent;

			// Get animation set for this player
			PlayerAnimations animations = playerAnimationsMap.get(node.getEntity().getID());

			// Determine animation state based on velocity
			Vector2D velocity = physics.getVelocity();
			boolean isMoving = velocity.magnitudeSquared() > 0.5f; // Small threshold to avoid flicker

			// Flip sprite based on horizontal movement direction
			if (velocity.getX() < -0.1f) {
				renderer.setFlipX(true);
			} else if (velocity.getX() > 0.1f) {
				renderer.setFlipX(false);
			}

			// Set appropriate animation
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
		playerAnimationsMap.put(entityId, animations);

		// Set initial animation
		SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);
		if (renderer != null) {
			renderer.setCurrentAnimation(animations.idleAnimation);
		}
	}

	private PlayerAnimations createPlayerAnimations() {
		ResourceManager resourceManager = ResourceManager.getInstance();

		String basePath = "0x72_DungeonTilesetII_v1.7/0x72_DungeonTilesetII_v1.7/frames/";

		// Create run animation
		List<Sprite> runFrames = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			Sprite frame = new Sprite("elf_run_" + i,
				resourceManager.loadImage(basePath + "elf_m_run_anim_f" + i + ".png"));
			runFrames.add(frame);
		}
		SpriteAnimation runAnimation = new SpriteAnimation(runFrames, GameConstants.DEFAULT_FRAME_DURATION, true);

		// For now, I'm just using the first frame as idle
		List<Sprite> idleFrames = new ArrayList<>();
		idleFrames.add(runFrames.get(0));
		SpriteAnimation idleAnimation = new SpriteAnimation(idleFrames, GameConstants.DEFAULT_FRAME_DURATION, true);

		return new PlayerAnimations(runAnimation, idleAnimation);
	}

	/**
	 * Used to keep the different animations for a player
	 */
	private record PlayerAnimations(
		SpriteAnimation runAnimation,
		SpriteAnimation idleAnimation) {
	}
}