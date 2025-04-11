package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.events.CollisionEvent;
import dk.sdu.sem.collision.events.CollisionEventType;
import dk.sdu.sem.collision.events.TriggerEvent;
import dk.sdu.sem.collisionsystem.detection.CollisionDetector;
import dk.sdu.sem.collisionsystem.resolution.CollisionResolver;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;

import java.util.*;

/**
 * Central physics system that handles collision detection and resolution.
 * Responsible for both regular and tilemap collisions, including triggers.
 */
public class CollisionSystem implements IFixedUpdate, ICollisionSPI {
	private final CollisionDetector detector;
	private final CollisionResolver resolver;
	private final EventDispatcher eventDispatcher;
	private final LayerCollisionMatrix layerMatrix;

	// Collision tracking for event handling
	private final Map<String, CollisionPair> activeCollisions = new HashMap<>();
	private final Map<String, TriggerPair> activeTriggers = new HashMap<>();

	public CollisionSystem() {
		this.layerMatrix = new LayerCollisionMatrix();
		this.detector = new CollisionDetector(layerMatrix);
		this.resolver = new CollisionResolver();
		this.eventDispatcher = new EventDispatcher();
	}

	@Override
	public void fixedUpdate() {
		// Get all colliders and tilemaps from the scene
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

		// Detect collisions
		List<CollisionPair> collisionPairs = detector.detectCollisions(colliderNodes);
		List<CollisionPair> tilemapCollisions = detector.detectTilemapCollisions(colliderNodes, tilemapNodes);

		// Combine all collision pairs
		List<CollisionPair> allCollisions = new ArrayList<>(collisionPairs);
		allCollisions.addAll(tilemapCollisions);

		// Process physical collisions and triggers separately
		List<CollisionPair> physicalCollisions = new ArrayList<>();
		List<TriggerPair> triggers = new ArrayList<>();

		// Separate into physical collisions and triggers
		for (CollisionPair pair : allCollisions) {
			if (pair.isTrigger()) {
				triggers.add(new TriggerPair(pair));
			} else {
				physicalCollisions.add(pair);
			}
		}

		// Step 1: Resolve physical collisions
		resolver.resolveCollisions(new HashSet<>(physicalCollisions));

		// Step 2: Generate and dispatch collision events
		processCollisionEvents(physicalCollisions);

		// Step 3: Generate and dispatch trigger events
		processTriggerEvents(triggers);
	}

	/**
	 * Process physical collision events (Enter, Stay, Exit)
	 */
	private void processCollisionEvents(List<CollisionPair> currentCollisions) {
		// Track current collision IDs to detect exits
		Set<String> currentCollisionIds = new HashSet<>();

		// Process current collisions (Enter or Stay)
		for (CollisionPair pair : currentCollisions) {
			String pairId = pair.getId();
			currentCollisionIds.add(pairId);

			CollisionEvent event;
			if (activeCollisions.containsKey(pairId)) {
				// Collision was already active - it's a STAY event
				event = new CollisionEvent(
					CollisionEventType.STAY,
					pair.getEntityA(),
					pair.getEntityB(),
					pair.getContact()
				);
				// Update the stored collision with current contact info
				activeCollisions.put(pairId, pair);
			} else {
				// New collision - it's an ENTER event
				event = new CollisionEvent(
					CollisionEventType.ENTER,
					pair.getEntityA(),
					pair.getEntityB(),
					pair.getContact()
				);
				activeCollisions.put(pairId, pair);
			}

			// Dispatch the event
			eventDispatcher.dispatchCollisionEvent(event);
		}

		// Find ended collisions and dispatch EXIT events
		Set<String> endedCollisions = new HashSet<>(activeCollisions.keySet());
		endedCollisions.removeAll(currentCollisionIds);

		for (String pairId : endedCollisions) {
			CollisionPair pair = activeCollisions.remove(pairId);

			CollisionEvent exitEvent = new CollisionEvent(
				CollisionEventType.EXIT,
				pair.getEntityA(),
				pair.getEntityB(),
				null // No contact point for exit events
			);

			eventDispatcher.dispatchCollisionEvent(exitEvent);
		}
	}

	/**
	 * Process trigger events (Enter, Stay, Exit)
	 */
	private void processTriggerEvents(List<TriggerPair> currentTriggers) {
		// Track current trigger IDs to detect exits
		Set<String> currentTriggerIds = new HashSet<>();

		// Process current triggers (Enter or Stay)
		for (TriggerPair pair : currentTriggers) {
			String pairId = pair.getId();
			currentTriggerIds.add(pairId);

			TriggerEvent event;
			if (activeTriggers.containsKey(pairId)) {
				// Trigger was already active - it's a STAY event
				event = new TriggerEvent(
					CollisionEventType.STAY,
					pair.getEntityA(),
					pair.getEntityB()
				);
			} else {
				// New trigger - it's an ENTER event
				event = new TriggerEvent(
					CollisionEventType.ENTER,
					pair.getEntityA(),
					pair.getEntityB()
				);
				activeTriggers.put(pairId, pair);
			}

			// Dispatch the event
			eventDispatcher.dispatchTriggerEvent(event);
		}

		// Find ended triggers and dispatch EXIT events
		Set<String> endedTriggers = new HashSet<>(activeTriggers.keySet());
		endedTriggers.removeAll(currentTriggerIds);

		for (String pairId : endedTriggers) {
			TriggerPair pair = activeTriggers.remove(pairId);

			TriggerEvent exitEvent = new TriggerEvent(
				CollisionEventType.EXIT,
				pair.getEntityA(),
				pair.getEntityB()
			);

			eventDispatcher.dispatchTriggerEvent(exitEvent);
		}
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		return detector.raycast(origin, direction, maxDistance);
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		return detector.raycast(origin, direction, maxDistance, layer);
	}

	@Override
	public boolean checkCollision(Entity a, Entity b) {
		ColliderComponent colliderA = a.getComponent(ColliderComponent.class);
		ColliderComponent colliderB = b.getComponent(ColliderComponent.class);

		if (colliderA == null || colliderB == null) {
			return false;
		}

		return detector.checkCollision(colliderA, colliderB);
	}

	@Override
	public void cleanupEntity(Entity entity) {
		// Remove all collision pairs involving this entity
		activeCollisions.entrySet().removeIf(entry -> {
			CollisionPair pair = entry.getValue();
			return pair.getEntityA() == entity || pair.getEntityB() == entity;
		});

		// Remove all trigger pairs involving this entity
		activeTriggers.entrySet().removeIf(entry -> {
			TriggerPair pair = entry.getValue();
			return pair.getEntityA() == entity || pair.getEntityB() == entity;
		});
	}

	@Override
	public boolean isPositionValid(ColliderComponent collider, Vector2D proposedPos) {
		return false;
	}
}