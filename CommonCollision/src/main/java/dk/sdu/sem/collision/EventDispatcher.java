package dk.sdu.sem.collision;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.events.CollisionEvent;
import dk.sdu.sem.collision.events.TriggerEvent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatches collision and trigger events to interested components.
 */
public class EventDispatcher {
	/**
	 * Dispatches a collision event to all ICollisionListener components on the entity.
	 *
	 * @param event The collision event to dispatch
	 */
	public void dispatchCollisionEvent(CollisionEvent event) {
		if (event.getEntity() == null || event.getOtherEntity() == null) {
			return;
		}

		Entity entity = event.getEntity();
		Entity other = event.getOtherEntity();
		ContactPoint contact = event.getContactPoint();

		// Create collision data
		List<ContactPoint> contacts = new ArrayList<>();
		if (contact != null) {
			contacts.add(contact);
		}

		// Calculate relative velocity (would normally come from physics system)
		Vector2D relativeVelocity = new Vector2D(0, 0);

		// Create collision object
		ColliderComponent otherCollider = other.getComponent(ColliderComponent.class);
		Collision collision = new Collision(other, otherCollider, contacts, relativeVelocity);

		// Find all ICollisionListener components
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ICollisionListener listener) {
				switch (event.getType()) {
					case ENTER:
						listener.onCollisionEnter(collision);
						break;
					case STAY:
						listener.onCollisionStay(collision);
						break;
					case EXIT:
						listener.onCollisionExit(collision);
						break;
				}
			}
		}
	}

	/**
	 * Dispatches a trigger event to all ITriggerListener components on the entity.
	 *
	 * @param event The trigger event to dispatch
	 */
	public void dispatchTriggerEvent(TriggerEvent event) {
		if (event.getEntity() == null || event.getOtherEntity() == null) {
			return;
		}

		Entity entity = event.getEntity();
		Entity other = event.getOtherEntity();

		// Find all ITriggerListener components
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ITriggerListener listener) {
				switch (event.getType()) {
					case ENTER:
						listener.onTriggerEnter(other);
						break;
					case STAY:
						listener.onTriggerStay(other);
						break;
					case EXIT:
						listener.onTriggerExit(other);
						break;
				}
			}
		}
	}
}