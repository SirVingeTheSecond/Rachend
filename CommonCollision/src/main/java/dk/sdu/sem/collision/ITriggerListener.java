package dk.sdu.sem.collision;

import dk.sdu.sem.collision.events.TriggerEnterEvent;
import dk.sdu.sem.collision.events.TriggerExitEvent;
import dk.sdu.sem.collision.events.TriggerStayEvent;

/**
 * Interface for components that want to receive trigger collision events.
 */
public interface ITriggerListener {
	/**
	 * Called when another collider enters this trigger.
	 *
	 * @param event Information about the trigger event
	 */
	void onTriggerEnter(TriggerEnterEvent event);

	/**
	 * Called once per fixed update for every collider that is in this trigger.
	 *
	 * @param event Information about the trigger event
	 */
	void onTriggerStay(TriggerStayEvent event);

	/**
	 * Called when another collider exits this trigger.
	 *
	 * @param event Information about the trigger event
	 */
	void onTriggerExit(TriggerExitEvent event);
}