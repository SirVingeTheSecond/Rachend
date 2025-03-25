package dk.sdu.sem.physicssystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.ColliderComponent;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Pair;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.*;

/**
 * System responsible for physics simulation with improved collision response.
 */
public class PhysicsSystem implements IFixedUpdate, IUpdate {
	private final Optional<ICollisionSPI> collisionService;
	private static final boolean DEBUG_PHYSICS = false;

	// Cache valid positions
	private final Map<Pair<ColliderComponent, Vector2D>, Boolean> positionValidCache = new HashMap<>();

	public PhysicsSystem() {
		// Try to load collision service
		Iterator<ICollisionSPI> services = ServiceLoader.load(ICollisionSPI.class).iterator();
		collisionService = services.hasNext() ? Optional.of(services.next()) : Optional.empty();

		if (collisionService.isPresent()) {
			System.out.println("Collision service loaded successfully");
		} else {
			System.out.println("No collision service available - physics will not check for collisions");
		}
	}

	@Override
	public void fixedUpdate() {
		// Clear cache each frame
		positionValidCache.clear();

		// Apply friction to all physics objects (regardless of collision)
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D velocity = node.physicsComponent.getVelocity();
			if (velocity.magnitudeSquared() < 0.001f)
				return;

			Vector2D friction = velocity
				.scale(node.physicsComponent.getFriction() * (float)Time.getFixedDeltaTime());

			Vector2D newVelocity = velocity.subtract(friction);
			if (newVelocity.magnitudeSquared() < 0.01f)
				newVelocity = new Vector2D(0,0);

			node.physicsComponent.setVelocity(newVelocity);
		});
	}

	@Override
	public void update() {
		// If collision service exists but entity doesn't have a collider,
		// or if collision module is absent, handle movement here

		// Only process non-collider entities here; entities with colliders are
		// handled in CollisionSystem's fixedUpdate

		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			// Skip entities with colliders - they're handled by the collision system
			if (collisionService.isPresent() && node.getEntity().hasComponent(ColliderComponent.class)) {
				return;
			}

			Vector2D currentPos = node.transform.getPosition();
			Vector2D velocity = node.physicsComponent.getVelocity();

			// Skip if not moving
			if (velocity.magnitudeSquared() < 0.001f) {
				return;
			}

			// IMPROVED: Apply movement directly for non-collision entities
			Vector2D displacement = velocity.scale((float) Time.getDeltaTime());
			Vector2D newPos = currentPos.add(displacement);

			if (DEBUG_PHYSICS) {
				System.out.printf("Physics: Moving from (%.2f, %.2f) to (%.2f, %.2f)\n",
					currentPos.getX(), currentPos.getY(), newPos.getX(), newPos.getY());
			}

			node.transform.setPosition(newPos);
		});
	}

	/**
	 * Helper method for component-wise movement testing.
	 * Tests if a movement along a single axis is valid.
	 *
	 * @param collider The collider component
	 * @param currentPos The current position
	 * @param proposedMovement The proposed movement vector
	 * @return true if the movement is valid, false otherwise
	 */
	public boolean isAxisMovementValid(ColliderComponent collider, Vector2D currentPos, Vector2D proposedMovement) {
		if (!collisionService.isPresent()) {
			return true; // No collision service, movement is valid
		}

		Vector2D proposedPos = currentPos.add(proposedMovement);

		// Create cache key
		Pair<ColliderComponent, Vector2D> cacheKey = Pair.of(collider, proposedPos);

		// Check cache first
		Boolean cached = positionValidCache.get(cacheKey);
		if (cached != null) {
			return cached;
		}

		// Calculate and cache result
		boolean isValid = collisionService.get().isPositionValid(collider, proposedPos);
		positionValidCache.put(cacheKey, isValid);

		return isValid;
	}
}