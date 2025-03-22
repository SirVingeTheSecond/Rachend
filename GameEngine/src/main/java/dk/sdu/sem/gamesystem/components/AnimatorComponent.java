package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.AnimationReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Component that controls animations for an entity.
 * Separates animation logic from rendering.
 */
public class AnimatorComponent implements IComponent {
	// Current animation state
	private String currentState;

	// Map of state names to animation references
	private final Map<String, AnimationReference> animations = new HashMap<>();

	// Parameters that can affect animation transitions
	private final Map<String, Object> parameters = new HashMap<>();

	// Transition rules, I think this should do for now
	private final Map<String, Map<String, Condition>> transitions = new HashMap<>();

	public AnimatorComponent() {
	}

	/**
	 * Adds an animation by ID.
	 */
	public void addAnimation(String stateName, String animationId) {
		addAnimation(stateName, new AnimationReference(animationId));
	}

	/**
	 * Adds an animation for a state.
	 */
	public void addAnimation(String stateName, AnimationReference animRef) {
		animations.put(stateName, animRef);

		// If this is the first animation, set it as current
		if (currentState == null) {
			currentState = stateName;
		}
	}

	/**
	 * Factory method for animator setup.
	 */
	public static AnimatorComponent createWithAnimations(String defaultState, Map<String, String> animations) {
		AnimatorComponent animator = new AnimatorComponent();

		for (Map.Entry<String, String> entry : animations.entrySet()) {
			String stateName = entry.getKey();
			String animationId = entry.getValue();
			animator.addAnimation(stateName, animationId);
		}

		if (defaultState != null && animations.containsKey(defaultState)) {
			animator.playState(defaultState);
		}

		return animator;
	}

	/**
	 * Sets a parameter value.
	 */
	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}

	/**
	 * Gets a parameter value.
	 */
	public Object getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Gets all parameters - used by animation system for evaluating transitions.
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Gets transitions for a specific state - used by animation system.
	 */
	public Map<String, Condition> getTransitionsForState(String state) {
		return transitions.getOrDefault(state, Map.of());
	}

	/**
	 * Adds a transition between states with a condition.
	 */
	public void addTransition(String fromState, String toState, Condition condition) {
		transitions.computeIfAbsent(fromState, k -> new HashMap<>())
			.put(toState, condition);
	}

	/**
	 * Gets the current state name.
	 */
	public String getCurrentState() {
		return currentState;
	}

	/**
	 * Forces a state change.
	 */
	public void playState(String stateName) {
		if (animations.containsKey(stateName)) {
			currentState = stateName;
		}
	}

	/**
	 * Gets the animation reference for the current state.
	 */
	public AnimationReference getCurrentAnimationReference() {
		return animations.get(currentState);
	}

	/**
	 * Interface for transition conditions.
	 */
	public interface Condition {
		boolean evaluate(Map<String, Object> parameters);
	}
}