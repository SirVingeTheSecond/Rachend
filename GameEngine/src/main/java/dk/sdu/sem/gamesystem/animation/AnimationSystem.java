package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.assets.AnimationReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.AnimatorNode;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Map;
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

	private void updateAnimator(AnimatorNode node) {
		AnimatorComponent animator = node.animator;
		SpriteRendererComponent renderer = node.renderer;

		// Check for state transitions
		String currentState = animator.getCurrentState();
		Map<String, AnimatorComponent.Condition> stateTransitions =
			animator.transitions.getOrDefault(currentState, Map.of());

		// Evaluate transitions
		for (Map.Entry<String, AnimatorComponent.Condition> transition : stateTransitions.entrySet()) {
			if (transition.getValue().evaluate(animator.parameters)) {
				animator.playState(transition.getKey());
				break;
			}
		}

		// Get current animation
		AnimationReference animRef = animator.getCurrentAnimationReference();

		// If animation changed, update the renderer
		if (renderer.getAnimationReference() != animRef) {
			renderer.setAnimationReference(animRef);
		}

		// Update current animation
		SpriteAnimation currentAnimation = renderer.getCurrentAnimation();
		if (currentAnimation != null) {
			currentAnimation.update(Time.getDeltaTime());
			renderer.setSprite(currentAnimation.getCurrentFrame());
		}
	}
}