package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.AnimatorNode;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System that updates animations based on animator components.
 */
public class AnimationSystem implements IUpdate {
	@Override
	public void update() {
		Set<AnimatorNode> nodes = NodeManager.active().getNodes(AnimatorNode.class);

		for (AnimatorNode node : nodes) {
			updateAnimator(node);
		}
	}

	/**
	 * Updates an animator node's animation state.
	 */
	private void updateAnimator(AnimatorNode node) {
		AnimatorComponent animator = node.animator;
		SpriteRendererComponent renderer = node.renderer;

		// Let the animator check transitions
		animator.update();

		// Get current animation
		SpriteAnimation animation = animator.getCurrentAnimation();

		// Update animation
		if (animation != null) {
			animation.update((float)Time.getDeltaTime());

			// Update sprite
			Sprite currentFrame = animation.getCurrentFrame();
			if (currentFrame != null) {
				renderer.setSprite(currentFrame.getName());
			}
		}
	}
}