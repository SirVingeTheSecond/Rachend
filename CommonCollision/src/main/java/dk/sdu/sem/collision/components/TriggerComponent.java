package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that implements ITriggerListener and provides a way to register
 * callback functions for trigger events.
 * <p>
 * This component can be added to entities that need to respond to trigger events
 * without creating a custom component each time.
 */
public class TriggerComponent implements IComponent, ITriggerListener {

	@FunctionalInterface
	public interface TriggerCallback {
		void onTrigger(Entity other);
	}

	private TriggerCallback onEnterCallback;
	private TriggerCallback onStayCallback;
	private TriggerCallback onExitCallback;

	/**
	 * Sets the callback for when another collider enters this trigger.
	 * @param callback The callback to invoke when a trigger enter event occurs
	 * @return This component for method chaining
	 */
	public TriggerComponent setOnTriggerEnter(TriggerCallback callback) {
		this.onEnterCallback = callback;
		return this;
	}

	/**
	 * Sets the callback for when another collider stays in this trigger.
	 * @param callback The callback to invoke when a trigger stay event occurs
	 * @return This component for method chaining
	 */
	public TriggerComponent setOnTriggerStay(TriggerCallback callback) {
		this.onStayCallback = callback;
		return this;
	}

	/**
	 * Sets the callback for when another collider exits this trigger.
	 * @param callback The callback to invoke when a trigger exit event occurs
	 * @return This component for method chaining
	 */
	public TriggerComponent setOnTriggerExit(TriggerCallback callback) {
		this.onExitCallback = callback;
		return this;
	}

	@Override
	public void onTriggerEnter(Entity other) {
		if (onEnterCallback != null) {
			onEnterCallback.onTrigger(other);
		}
	}

	@Override
	public void onTriggerStay(Entity other) {
		if (onStayCallback != null) {
			onStayCallback.onTrigger(other);
		}
	}

	@Override
	public void onTriggerExit(Entity other) {
		if (onExitCallback != null) {
			onExitCallback.onTrigger(other);
		}
	}
}