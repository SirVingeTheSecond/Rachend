package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Unity-like animation controller component.
 */
public class AnimatorComponent implements IComponent {
	// Current state
	private String currentState;

	// Animations for each state
	private final Map<String, SpriteAnimation> animations = new HashMap<>();

	// Affect transitions
	private final Map<String, Object> parameters = new HashMap<>();

	// Transitions between states
	private final Map<String, Map<String, Transition>> transitions = new HashMap<>();

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
		playState("default");
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
			animator.playState(defaultState);
		}

		return animator;
	}

	/**
	 * Adds an animation state.
	 */
	public void addState(String stateName, String animationName) {
		// Load the animation
		SpriteAnimation animation = AssetFacade.loadAnimation(animationName);
		animations.put(stateName, animation);

		// If first state, set as current
		if (currentState == null) {
			currentState = stateName;
		}
	}

	/**
	 * Unified parameter method - cleaner API
	 * Works with any parameter type.
	 */
	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}

	/**
	 * Get parameter value
	 */
	public Object getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Get parameter as boolean
	 */
	public boolean getBoolParameter(String name) {
		Object value = parameters.get(name);
		return value instanceof Boolean ? (Boolean)value : false;
	}

	/**
	 * Get parameter as float
	 */
	public float getFloatParameter(String name) {
		Object value = parameters.get(name);
		if (value instanceof Number) {
			return ((Number)value).floatValue();
		}
		return 0f;
	}

	/**
	 * Get parameter as int
	 */
	public int getIntParameter(String name) {
		Object value = parameters.get(name);
		if (value instanceof Number) {
			return ((Number)value).intValue();
		}
		return 0;
	}

	/**
	 * Adds a simple transition between states based on a boolean parameter.
	 */
	public void addTransition(String fromState, String toState, String paramName, boolean value) {
		addTransition(fromState, toState, params -> {
			Object paramValue = params.get(paramName);
			return paramValue instanceof Boolean && (Boolean)paramValue == value;
		});
	}

	/**
	 * Adds a transition between states with a custom condition.
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
	 * Plays a state immediately.
	 */
	public void playState(String stateName) {
		if (animations.containsKey(stateName)) {
			currentState = stateName;
			SpriteAnimation animation = animations.get(stateName);
			if (animation != null) {
				animation.reset();
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
	 * Updates the animator, checking for transitions.
	 * Called by the animation system.
	 */
	public void update() {
		if (currentState == null) return;

		// Check for transitions
		Map<String, Transition> stateTransitions = transitions.get(currentState);
		if (stateTransitions != null) {
			for (Map.Entry<String, Transition> entry : stateTransitions.entrySet()) {
				if (entry.getValue().condition.test(parameters)) {
					playState(entry.getKey());
					break;
				}
			}
		}
	}

	/**
	 * Represents a transition between animation states.
	 */
	private static class Transition {
		final Predicate<Map<String, Object>> condition;

		Transition(Predicate<Map<String, Object>> condition) {
			this.condition = condition;
		}
	}
}