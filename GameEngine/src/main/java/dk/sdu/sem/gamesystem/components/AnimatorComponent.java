package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Animation controller component.
 */
public class AnimatorComponent implements IComponent {
	// Current state
	private String currentState;

	// Animations for each state
	private final Map<String, SpriteAnimation> animations = new HashMap<>();

	// Parameters used for transitions
	private final Map<String, Object> parameters = new HashMap<>();

	// Transitions between states
	private final Map<String, Map<String, Transition>> transitions = new HashMap<>();

	// One-shot animation data
	private String oneShotAnimation;
	private String returnState;

	// Flags
	private boolean isOneShotPending = false;
	private boolean playReversed = false;

	/**
	 * Creates an empty animator component.
	 */
	public AnimatorComponent() {
	}

	/**
	 * Creates an animator with default animation.
	 *
	 * @param defaultAnimationName Name of the default animation
	 */
	public AnimatorComponent(String defaultAnimationName) {
		addState("default", defaultAnimationName);
		setCurrentState("default");
	}

	/**
	 * Creates an animator with initial states.
	 */
	public static AnimatorComponent create(String defaultState, Map<String, String> stateAnimations) {
		AnimatorComponent animator = new AnimatorComponent();

		// Add all states
		for (Map.Entry<String, String> entry : stateAnimations.entrySet()) {
			animator.addState(entry.getKey(), entry.getValue());
		}

		// Set initial state
		if (defaultState != null && stateAnimations.containsKey(defaultState)) {
			animator.setCurrentState(defaultState);
		}

		return animator;
	}

	/**
	 * Adds an animation state.
	 */
	public void addState(String stateName, String animationName) {
		// Load the animation
		SpriteAnimation animation = AssetFacade.getAnimation(animationName);
		animations.put(stateName, animation);

		// If first state, set as current
		if (currentState == null) {
			currentState = stateName;
		}
	}

	/**
	 * Gets a parameter value.
	 */
	public Object getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Gets the parameters map.
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Sets a parameter value.
	 */
	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}

	/**
	 * Adds a transition between states based on a boolean parameter.
	 */
	public void addTransition(String fromState, String toState, String paramName, boolean value) {
		addTransition(fromState, toState, params -> {
			Object paramValue = params.get(paramName);
			return paramValue instanceof Boolean && (Boolean)paramValue == value;
		});
	}

	/**
	 * Adds a transition between states with a condition.
	 */
	public void addTransition(String fromState, String toState, Predicate<Map<String, Object>> condition) {
		transitions.computeIfAbsent(fromState, k -> new HashMap<>())
			.put(toState, new Transition(condition));
	}

	/**
	 * Gets the current state.
	 */
	public String getCurrentState() {
		return currentState;
	}

	/**
	 * Sets the current state.
	 */
	public void setCurrentState(String stateName) {
		if (animations.containsKey(stateName)) {
			currentState = stateName;
			SpriteAnimation animation = animations.get(stateName);
			if (animation != null) {
				// Apply reverse playback setting to the new animation
				animation.reset();
				animation.play(playReversed);
			}
		}
	}

	/**
	 * Gets the current animation.
	 */
	public SpriteAnimation getCurrentAnimation() {
		return animations.get(currentState);
	}

	/**
	 * Gets the animation for a specific state.
	 */
	public SpriteAnimation getAnimation(String stateName) {
		return animations.get(stateName);
	}

	/**
	 * Gets all available animations.
	 */
	public Map<String, SpriteAnimation> getAnimations() {
		return animations;
	}

	/**
	 * Gets all transitions for the current state.
	 */
	public Map<String, Transition> getTransitionsForCurrentState() {
		return transitions.get(currentState);
	}

	/**
	 * Gets all transitions.
	 */
	public Map<String, Map<String, Transition>> getTransitions() {
		return transitions;
	}

	/**
	 * Sets data for playing a one-shot animation.
	 */
	public void setOneShotData(String animationState, String returnToState) {
		if (animations.containsKey(animationState) && animations.containsKey(returnToState)) {
			this.oneShotAnimation = animationState;
			this.returnState = returnToState;
			this.isOneShotPending = true;
		}
	}

	/**
	 * Clears one-shot animation data.
	 */
	public void clearOneShotData() {
		this.oneShotAnimation = null;
		this.isOneShotPending = false;
	}

	/**
	 * Checks if a one-shot animation is pending.
	 */
	public boolean isOneShotPending() {
		return isOneShotPending;
	}

	/**
	 * Gets the pending one-shot animation state.
	 */
	public String getOneShotAnimation() {
		return oneShotAnimation;
	}

	/**
	 * Gets the state to return to after one-shot animation.
	 */
	public String getReturnState() {
		return returnState;
	}

	/**
	 * Sets whether the animations should play in reverse.
	 * Applies to all animations managed by this component.
	 *
	 * @param reverse True to play in reverse, false to play in forward
	 */
	public void setPlayReversed(boolean reverse) {
		this.playReversed = reverse;

		// Apply to current animation immediately
		SpriteAnimation currentAnimation = getCurrentAnimation();
		if (currentAnimation != null) {
			currentAnimation.play(reverse);
		}
	}

	/**
	 * Checks if animations are set to play in reverse.
	 *
	 * @return True if set to play in reverse, false if set to play forward
	 */
	public boolean isPlayReversed() {
		return playReversed;
	}

	/**
	 * Flips the playback direction of all animations.
	 * If currently playing forward, will play in reverse and vice versa.
	 */
	public void flipPlaybackDirection() {
		this.playReversed = !this.playReversed;

		// Apply to current animation immediately
		SpriteAnimation currentAnimation = getCurrentAnimation();
		if (currentAnimation != null) {
			currentAnimation.flipDirection();
		}
	}

	/**
	 * Sets data for playing a one-shot animation with specified direction.
	 *
	 * @param animationState The animation state to play
	 * @param returnToState The state to return to when complete
	 * @param reverse True to play in reverse, false to play forward
	 */
	public void setOneShotData(String animationState, String returnToState, boolean reverse) {
		if (animations.containsKey(animationState) && animations.containsKey(returnToState)) {
			this.oneShotAnimation = animationState;
			this.returnState = returnToState;
			this.isOneShotPending = true;

			// Pre-set the direction for when the animation is applied
			SpriteAnimation animation = animations.get(animationState);
			if (animation != null) {
				animation.play(reverse);
			}
		}
	}

	/**
	 * Represents a transition between animation states.
	 */
	public static class Transition {
		private final Predicate<Map<String, Object>> condition;

		public Transition(Predicate<Map<String, Object>> condition) {
			this.condition = condition;
		}

		public boolean evaluate(Map<String, Object> parameters) {
			return condition.test(parameters);
		}
	}
}