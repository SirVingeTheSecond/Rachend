package dk.sdu.sem.collisionsystem.systems;

import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.data.TriggerPair;
import dk.sdu.sem.collision.events.*;
import dk.sdu.sem.collisionsystem.events.EventSystem;
import dk.sdu.sem.collisionsystem.state.CollisionState;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;

import java.util.HashSet;
import java.util.Set;

/**
 * System responsible for generating and dispatching collision events.
 * Compares current collisions with previous collisions to determine
 * ENTER, STAY, and EXIT events.
 */
public class CollisionEventSystem {
	// Collision state
	private final CollisionState collisionState;

	// Event system
	private final IEventSystem eventSystem;

	/**
	 * Creates a new collision event system.
	 *
	 * @param collisionState The shared collision state
	 */
	public CollisionEventSystem(CollisionState collisionState) {
		this.collisionState = collisionState;
		this.eventSystem = EventSystem.getInstance();
	}

	/**
	 * Processes collision events for the current frame.
	 */
	public void process() {
		Set<CollisionPair> currentCollisions = collisionState.getCurrentCollisions();
		Set<TriggerPair> currentTriggers = collisionState.getCurrentTriggers();

		// Process physical collisions
		processCollisionEvents(currentCollisions);

		// Process trigger collisions
		processTriggerEvents(currentTriggers);
	}

	/**
	 * Processes physical collision events (Enter, Stay, Exit).
	 */
	private void processCollisionEvents(Set<CollisionPair> currentCollisions) {
		// Create a set of current collision IDs for easy lookup
		Set<String> currentCollisionIds = new HashSet<>();
		for (CollisionPair pair : currentCollisions) {
			currentCollisionIds.add(pair.getId());

			// Get or create collision state components
			CollisionStateComponent stateA = getOrCreateCollisionState(pair.getEntityA());
			CollisionStateComponent stateB = getOrCreateCollisionState(pair.getEntityB());

			// Determine if this is an ENTER or STAY event
			if (stateA.hasCollision(pair.getId())) {
				// Collision was already active - it's a STAY event
				dispatchCollisionStayEvents(pair);

				// Update collision info in state
				stateA.addCollision(pair);
				stateB.addCollision(pair);
			} else {
				// New collision - it's an ENTER event
				dispatchCollisionEnterEvents(pair);

				// Store for next frame
				stateA.addCollision(pair);
				stateB.addCollision(pair);
			}
		}

		// Find ended collisions for both entities in each pair
		for (Entity entity : getEntitiesWithCollisionState()) {
			CollisionStateComponent state = entity.getComponent(CollisionStateComponent.class);
			if (state == null) continue;

			// Get a copy of active collisions to avoid concurrent modification
			var activeCollisions = new HashSet<>(state.getActiveCollisions().entrySet());

			// Check each active collision to see if it's still active
			for (var entry : activeCollisions) {
				String pairId = entry.getKey();
				CollisionPair pair = entry.getValue();

				// If not in current collisions, it's an EXIT event
				if (!currentCollisionIds.contains(pairId)) {
					// Dispatch EXIT events
					dispatchCollisionExitEvents(pair);

					// Remove from state
					state.removeCollision(pairId);

					// Also remove from the other entity's state
					Entity other = (pair.getEntityA() == entity) ? pair.getEntityB() : pair.getEntityA();
					CollisionStateComponent otherState = other.getComponent(CollisionStateComponent.class);
					if (otherState != null) {
						otherState.removeCollision(pairId);
					}
				}
			}
		}
	}

	/**
	 * Processes trigger events (Enter, Stay, Exit).
	 */
	private void processTriggerEvents(Set<TriggerPair> currentTriggers) {
		// Create a set of current trigger IDs for easy lookup
		Set<String> currentTriggerIds = new HashSet<>();
		for (TriggerPair pair : currentTriggers) {
			currentTriggerIds.add(pair.getId());

			// Get or create collision state components
			CollisionStateComponent stateA = getOrCreateCollisionState(pair.getEntityA());
			CollisionStateComponent stateB = getOrCreateCollisionState(pair.getEntityB());

			// Determine if this is an ENTER or STAY event
			if (stateA.hasTrigger(pair.getId())) {
				// Trigger was already active - it's a STAY event
				dispatchTriggerStayEvents(pair);

				// Already stored from previous frame
			} else {
				// New trigger - it's an ENTER event
				dispatchTriggerEnterEvents(pair);

				// Store for next frame
				stateA.addTrigger(pair);
				stateB.addTrigger(pair);
			}
		}

		// Find ended triggers for both entities in each pair
		for (Entity entity : getEntitiesWithCollisionState()) {
			CollisionStateComponent state = entity.getComponent(CollisionStateComponent.class);
			if (state == null) continue;

			// Get a copy of active triggers to avoid concurrent modification
			var activeTriggers = new HashSet<>(state.getActiveTriggers().entrySet());

			// Check each active trigger to see if it's still active
			for (var entry : activeTriggers) {
				String pairId = entry.getKey();
				TriggerPair pair = entry.getValue();

				// If not in current triggers, it's an EXIT event
				if (!currentTriggerIds.contains(pairId)) {
					// Dispatch EXIT events
					dispatchTriggerExitEvents(pair);

					// Remove from state
					state.removeTrigger(pairId);

					// Also remove from the other entity's state
					Entity other = (pair.getEntityA() == entity) ? pair.getEntityB() : pair.getEntityA();
					CollisionStateComponent otherState = other.getComponent(CollisionStateComponent.class);
					if (otherState != null) {
						otherState.removeTrigger(pairId);
					}
				}
			}
		}
	}

	/**
	 * Dispatches collision enter events for both entities in a collision pair.
	 */
	private void dispatchCollisionEnterEvents(CollisionPair pair) {
		// Debug entity IDs and event creation
		System.out.println("Creating collision enter events for pair:");
		System.out.println("  EntityA: " + pair.getEntityA().getID());
		System.out.println("  EntityB: " + pair.getEntityB().getID());

		// Create and dispatch event for entity A
		CollisionEnterEvent eventA = new CollisionEnterEvent(
			pair.getEntityA(),
			pair.getEntityB(),
			pair.getContact()
		);
		System.out.println("  Publishing event for EntityA");
		eventSystem.publish(eventA);

		// Create and dispatch event for entity B (reversed contact normal)
		ContactPoint reversedContact = null;
		if (pair.getContact() != null) {
			reversedContact = new ContactPoint(
				pair.getContact().getPoint(),
				pair.getContact().getNormal().scale(-1),
				pair.getContact().getSeparation()
			);
		}

		CollisionEnterEvent eventB = new CollisionEnterEvent(
			pair.getEntityB(),
			pair.getEntityA(),
			reversedContact
		);
		System.out.println("  Publishing event for EntityB");
		eventSystem.publish(eventB);
	}

	/**
	 * Dispatches collision stay events for both entities in a collision pair.
	 */
	private void dispatchCollisionStayEvents(CollisionPair pair) {
		// Create and dispatch event for entity A
		CollisionStayEvent eventA = new CollisionStayEvent(
			pair.getEntityA(),
			pair.getEntityB(),
			pair.getContact()
		);
		eventSystem.publish(eventA);

		// Create and dispatch event for entity B (with reversed contact normal)
		ContactPoint reversedContact = null;
		if (pair.getContact() != null) {
			reversedContact = new ContactPoint(
				pair.getContact().getPoint(),
				pair.getContact().getNormal().scale(-1),
				pair.getContact().getSeparation()
			);
		}

		CollisionStayEvent eventB = new CollisionStayEvent(
			pair.getEntityB(),
			pair.getEntityA(),
			reversedContact
		);
		eventSystem.publish(eventB);
	}

	/**
	 * Dispatches collision exit events for both entities in a collision pair.
	 */
	private void dispatchCollisionExitEvents(CollisionPair pair) {
		// Create and dispatch event for entity A
		CollisionExitEvent eventA = new CollisionExitEvent(
			pair.getEntityA(),
			pair.getEntityB(),
			pair.getContact()
		);
		eventSystem.publish(eventA);

		// Create and dispatch event for entity B (with reversed contact normal)
		ContactPoint reversedContact = null;
		if (pair.getContact() != null) {
			reversedContact = new ContactPoint(
				pair.getContact().getPoint(),
				pair.getContact().getNormal().scale(-1),
				pair.getContact().getSeparation()
			);
		}

		CollisionExitEvent eventB = new CollisionExitEvent(
			pair.getEntityB(),
			pair.getEntityA(),
			reversedContact
		);
		eventSystem.publish(eventB);
	}

	/**
	 * Dispatches trigger enter events for both entities in a trigger pair.
	 */
	private void dispatchTriggerEnterEvents(TriggerPair pair) {
		// Create and dispatch event for entity A
		TriggerEnterEvent eventA = new TriggerEnterEvent(
			pair.getEntityA(),
			pair.getEntityB()
		);
		eventSystem.publish(eventA);

		// Create and dispatch event for entity B
		TriggerEnterEvent eventB = new TriggerEnterEvent(
			pair.getEntityB(),
			pair.getEntityA()
		);
		eventSystem.publish(eventB);
	}

	/**
	 * Dispatches trigger stay events for both entities in a trigger pair.
	 */
	private void dispatchTriggerStayEvents(TriggerPair pair) {
		// Create and dispatch event for entity A
		TriggerStayEvent eventA = new TriggerStayEvent(
			pair.getEntityA(),
			pair.getEntityB()
		);
		eventSystem.publish(eventA);

		// Create and dispatch event for entity B
		TriggerStayEvent eventB = new TriggerStayEvent(
			pair.getEntityB(),
			pair.getEntityA()
		);
		eventSystem.publish(eventB);
	}

	/**
	 * Dispatches trigger exit events for both entities in a trigger pair.
	 */
	private void dispatchTriggerExitEvents(TriggerPair pair) {
		// Create and dispatch event for entity A
		TriggerExitEvent eventA = new TriggerExitEvent(
			pair.getEntityA(),
			pair.getEntityB()
		);
		eventSystem.publish(eventA);

		// Create and dispatch event for entity B
		TriggerExitEvent eventB = new TriggerExitEvent(
			pair.getEntityB(),
			pair.getEntityA()
		);
		eventSystem.publish(eventB);
	}

	/**
	 * Gets or creates a collision state component for an entity.
	 */
	private CollisionStateComponent getOrCreateCollisionState(Entity entity) {
		CollisionStateComponent state = entity.getComponent(CollisionStateComponent.class);
		if (state == null) {
			state = new CollisionStateComponent();
			entity.addComponent(state);
		}
		return state;
	}

	/**
	 * Gets all entities with a collision state component.
	 */
	private Set<Entity> getEntitiesWithCollisionState() {
		if (Scene.getActiveScene() == null) {
			return new HashSet<>();
		}
		return Scene.getActiveScene().getEntitiesWithComponent(CollisionStateComponent.class);
	}

	/**
	 * Cleans up collision state for a destroyed entity.
	 *
	 * @param entity The entity to clean up
	 */
	public void cleanupEntity(Entity entity) {
		// Find any entities that have collision state involving this entity
		for (Entity other : getEntitiesWithCollisionState()) {
			CollisionStateComponent state = other.getComponent(CollisionStateComponent.class);
			if (state != null) {
				state.removeEntityCollisions(entity);
			}
		}
	}
}