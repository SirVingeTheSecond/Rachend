package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.AnimatorNode;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * System that updates animations based on animator components.
 * Uses the unified reference system for consistent sprite handling.
 */
public class AnimationSystem implements IUpdate {
	// Map to track one-shot animation callbacks
	private final Map<AnimatorNode, Runnable> completionCallbacks = new HashMap<>();

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

		// Check for pending one-shot animations
		if (animator.isOneShotPending()) {
			String oneShotState = animator.getOneShotAnimation();
			animator.setCurrentState(oneShotState);
			animator.clearOneShotData();
		}

		// Check for state transitions
		checkTransitions(animator);

		// Get current animation
		SpriteAnimation animation = animator.getCurrentAnimation();

		// Update animation
		if (animation != null) {
			animation.update((float)Time.getDeltaTime());

			// Update sprite using frame reference
			// This is the key change - pass the reference rather than resolving to name or sprite
			renderer.setSprite(animation.getCurrentFrameReference());

			// Check if a non-looping animation has finished
			if (!animation.isLooping() && animation.isFinished()) {
				handleCompletedAnimation(node, animator);
			}
		}
	}

	/**
	 * Checks and applies state transitions based on parameters.
	 */
	private void checkTransitions(AnimatorComponent animator) {
		Map<String, AnimatorComponent.Transition> stateTransitions =
			animator.getTransitionsForCurrentState();

		if (stateTransitions != null) {
			for (Map.Entry<String, AnimatorComponent.Transition> entry : stateTransitions.entrySet()) {
				if (entry.getValue().evaluate(animator.getParameters())) {
					animator.setCurrentState(entry.getKey());
					break;
				}
			}
		}
	}

	/**
	 * Handles a completed one-shot animation.
	 */
	private void handleCompletedAnimation(AnimatorNode node, AnimatorComponent animator) {
		// If this was a one-shot animation with a return state
		String returnState = animator.getReturnState();
		if (returnState != null && !returnState.equals(animator.getCurrentState())) {
			animator.setCurrentState(returnState);
		}

		// Execute any completion callback
		Runnable callback = completionCallbacks.remove(node);
		if (callback != null) {
			callback.run();
		}

		// Set a parameter to indicate completion - useful for transitions
		animator.setParameter("animationCompleted", true);
	}

	/**
	 * Plays a one-shot animation on an entity.
	 *
	 * @param node The animator node
	 * @param animationState The animation state to play
	 * @param returnToState The state to return to when complete
	 * @param callback Optional callback to execute when complete (can be null)
	 */
	public void playOneShot(AnimatorNode node, String animationState,
							String returnToState, Runnable callback) {
		AnimatorComponent animator = node.animator;

		// Set one-shot data
		animator.setOneShotData(animationState, returnToState);

		// Store callback if provided
		if (callback != null) {
			completionCallbacks.put(node, callback);
		}
	}

	/**
	 * Plays a one-shot animation that returns to the current state.
	 *
	 * @param node The animator node
	 * @param animationState The animation state to play
	 * @param callback Optional callback to execute when complete (can be null)
	 */
	public void playOneShot(AnimatorNode node, String animationState, Runnable callback) {
		AnimatorComponent animator = node.animator;
		String currentState = animator.getCurrentState();
		playOneShot(node, animationState, currentState, callback);
	}
}