package dk.sdu.sem.collisionsystem.events;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatches collision and trigger events to interested components.
 * Streamlined implementation with clear separation of concerns.
 */
public class EventDispatcher {
	/**
	 * Dispatches a collision event for a collision pair.
	 *
	 * @param pair The collision pair
	 * @param eventType The type of event (ENTER, STAY, EXIT)
	 */
	public void dispatchCollisionEvent(CollisionPair pair, CollisionEventType eventType) {
		Entity entityA = pair.getEntityA();
		Entity entityB = pair.getEntityB();

		if (entityA == null || entityB == null) {
			return;
		}

		// Create collision data for entity A
		Collision collisionForA = createCollision(entityB, pair.getColliderB(), pair.getContact());

		// Dispatch to entity A's listeners
		dispatchToCollisionListeners(entityA, collisionForA, eventType);

		// Create collision data for entity B (with reversed direction)
		ContactPoint reversedContact = null;
		if (pair.getContact() != null) {
			reversedContact = new ContactPoint(
				pair.getContact().getPoint(),
				pair.getContact().getNormal().scale(-1), // Reverse normal
				pair.getContact().getSeparation()
			);
		}

		Collision collisionForB = createCollision(entityA, pair.getColliderA(), reversedContact);

		// Dispatch to entity B's listeners
		dispatchToCollisionListeners(entityB, collisionForB, eventType);
	}

	/**
	 * Creates a Collision object with appropriate data.
	 */
	private Collision createCollision(Entity otherEntity, ColliderComponent otherCollider, ContactPoint contact) {
		List<ContactPoint> contacts = new ArrayList<>();
		if (contact != null) {
			contacts.add(contact);
		}

		// Relative velocity would ideally come from physics components
		Vector2D relativeVelocity = new Vector2D(0, 0);

		return new Collision(otherEntity, otherCollider, contacts, relativeVelocity);
	}

	/**
	 * Dispatches a collision event to all ICollisionListener components on an entity.
	 */
	private void dispatchToCollisionListeners(Entity entity, Collision collision, CollisionEventType eventType) {
		if (entity == null || entity.getScene() == null) {
			return;
		}

		// Find all ICollisionListener components
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ICollisionListener listener) {
				switch (eventType) {
					case ENTER -> listener.onCollisionEnter(collision);
					case STAY -> listener.onCollisionStay(collision);
					case EXIT -> listener.onCollisionExit(collision);
				}
			}
		}
	}

	/**
	 * Dispatches a trigger event between two entities.
	 *
	 * @param entity The entity receiving the event
	 * @param other The other entity involved
	 * @param eventType The type of event (ENTER, STAY, EXIT)
	 */
	public void dispatchTriggerEvent(Entity entity, Entity other, CollisionEventType eventType) {
		if (entity == null || other == null || entity.getScene() == null) {
			return;
		}

		// Find all ITriggerListener components
		for (IComponent component : entity.getAllComponents()) {
			if (component instanceof ITriggerListener listener) {
				switch (eventType) {
					case ENTER -> listener.onTriggerEnter(other);
					case STAY -> listener.onTriggerStay(other);
					case EXIT -> listener.onTriggerExit(other);
				}
			}
		}
	}
}