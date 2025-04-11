package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collisionsystem.broadphase.QuadTreeBroadphase;
import dk.sdu.sem.collisionsystem.events.CollisionEventType;
import dk.sdu.sem.collisionsystem.events.EventDispatcher;
import dk.sdu.sem.collisionsystem.narrowphase.NarrowPhaseDetector;
import dk.sdu.sem.collisionsystem.raycasting.RaycastHandler;
import dk.sdu.sem.collisionsystem.resolution.CollisionResolver;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;

import java.util.*;

/**
 * Central physics system that handles collision detection and resolution.
 * Now uses a unified approach to handle all collider types consistently.
 */
public class CollisionSystem implements IFixedUpdate, ICollisionSPI {
	private final LayerCollisionMatrix layerMatrix;
	private final QuadTreeBroadphase broadphase;
	private final NarrowPhaseDetector narrowphase;
	private final CollisionResolver resolver;
	private final EventDispatcher eventDispatcher;
	private final RaycastHandler raycastHandler;

	// State tracking for continuous collision detection
	private final Map<String, CollisionPair> activeCollisions = new HashMap<>();
	private final Map<String, TriggerPair> activeTriggers = new HashMap<>();

	public CollisionSystem() {
		this.layerMatrix = new LayerCollisionMatrix();
		this.broadphase = new QuadTreeBroadphase();
		this.narrowphase = new NarrowPhaseDetector(layerMatrix);
		this.resolver = new CollisionResolver();
		this.eventDispatcher = new EventDispatcher();
		this.raycastHandler = new RaycastHandler();

		System.out.println("CollisionSystem initialized with unified collider handling");
	}

	@Override
	public void fixedUpdate() {
		try {
			// Get all collider nodes (now includes both standard and tilemap colliders)
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

			// STEP 1: Broadphase - Find potential collision pairs
			Set<CollisionPair> potentialCollisions = broadphase.findPotentialCollisions(colliderNodes);

			// STEP 2: Narrowphase - Determine actual collisions with contact info
			Set<CollisionPair> allCollisions = narrowphase.detectCollisions(potentialCollisions);

			// STEP 3: Separate physical collisions from triggers
			Set<CollisionPair> physicalCollisions = new HashSet<>();
			Set<TriggerPair> triggerCollisions = new HashSet<>();

			for (CollisionPair pair : allCollisions) {
				if (pair.isTrigger()) {
					triggerCollisions.add(new TriggerPair(pair));
				} else {
					physicalCollisions.add(pair);
				}
			}

			// STEP 4: Resolve physical collisions
			resolver.resolveCollisions(physicalCollisions);

			// STEP 5: Process and dispatch collision events
			processCollisionEvents(physicalCollisions);

			// STEP 6: Process and dispatch trigger events
			processTriggerEvents(triggerCollisions);
		} catch (Exception e) {
			System.err.println("Error in CollisionSystem.fixedUpdate: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Processes physical collision events (Enter, Stay, Exit)
	 */
	private void processCollisionEvents(Set<CollisionPair> currentCollisions) {
		// Track current collision IDs to detect exits
		Set<String> currentCollisionIds = new HashSet<>();

		// Process current collisions (Enter or Stay)
		for (CollisionPair pair : currentCollisions) {
			String pairId = pair.getId();
			currentCollisionIds.add(pairId);

			if (activeCollisions.containsKey(pairId)) {
				// Collision was already active - it's a STAY event
				eventDispatcher.dispatchCollisionEvent(pair, CollisionEventType.STAY);
				// Update the stored collision with current contact info
				activeCollisions.put(pairId, pair);
			} else {
				// New collision - it's an ENTER event
				eventDispatcher.dispatchCollisionEvent(pair, CollisionEventType.ENTER);
				activeCollisions.put(pairId, pair);
			}
		}

		// Find ended collisions and dispatch EXIT events
		Set<String> endedCollisions = new HashSet<>(activeCollisions.keySet());
		endedCollisions.removeAll(currentCollisionIds);

		for (String pairId : endedCollisions) {
			CollisionPair pair = activeCollisions.remove(pairId);
			eventDispatcher.dispatchCollisionEvent(pair, CollisionEventType.EXIT);
		}
	}

	/**
	 * Processes trigger events (Enter, Stay, Exit)
	 */
	private void processTriggerEvents(Set<TriggerPair> currentTriggers) {
		// Track current trigger IDs to detect exits
		Set<String> currentTriggerIds = new HashSet<>();

		// Process current triggers (Enter or Stay)
		for (TriggerPair pair : currentTriggers) {
			String pairId = pair.getId();
			currentTriggerIds.add(pairId);

			if (activeTriggers.containsKey(pairId)) {
				// Trigger was already active - it's a STAY event
				eventDispatcher.dispatchTriggerEvent(pair.getEntityA(), pair.getEntityB(), CollisionEventType.STAY);
				eventDispatcher.dispatchTriggerEvent(pair.getEntityB(), pair.getEntityA(), CollisionEventType.STAY);
			} else {
				// New trigger - it's an ENTER event
				eventDispatcher.dispatchTriggerEvent(pair.getEntityA(), pair.getEntityB(), CollisionEventType.ENTER);
				eventDispatcher.dispatchTriggerEvent(pair.getEntityB(), pair.getEntityA(), CollisionEventType.ENTER);
				// Store for next frame
				activeTriggers.put(pairId, pair);
			}
		}

		// Find ended triggers and dispatch EXIT events
		Set<String> endedTriggers = new HashSet<>(activeTriggers.keySet());
		endedTriggers.removeAll(currentTriggerIds);

		for (String pairId : endedTriggers) {
			TriggerPair pair = activeTriggers.remove(pairId);
			eventDispatcher.dispatchTriggerEvent(pair.getEntityA(), pair.getEntityB(), CollisionEventType.EXIT);
			eventDispatcher.dispatchTriggerEvent(pair.getEntityB(), pair.getEntityA(), CollisionEventType.EXIT);
		}
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		return raycastHandler.raycast(origin, direction, maxDistance);
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		return raycastHandler.raycast(origin, direction, maxDistance, layer);
	}

	@Override
	public boolean checkCollision(Entity a, Entity b) {
		ColliderComponent colliderA = a.getComponent(ColliderComponent.class);
		ColliderComponent colliderB = b.getComponent(ColliderComponent.class);

		if (colliderA == null || colliderB == null) {
			return false;
		}

		return narrowphase.checkCollision(colliderA, colliderB);
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
		return narrowphase.isPositionValid(collider, proposedPos);
	}
}