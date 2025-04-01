package dk.sdu.sem.physicssystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Pair;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.*;

/**
 * System responsible for physics simulation.
 */
public class PhysicsSystem implements IFixedUpdate, IUpdate {
	private final Optional<ICollisionSPI> collisionService;
	private static final boolean DEBUG_PHYSICS = false;
	private static final float MIN_MOVEMENT_THRESHOLD = 0.001f;
	private static final float VELOCITY_RESET_THRESHOLD = 0.01f;

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
		// Clear cache at start of each fixed update
		positionValidCache.clear();

		// Apply friction to all physics objects
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			applyFriction(node);
		});
	}

	@Override
	public void update() {
		// Process all physics nodes for movement
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D currentPos = node.transform.getPosition();
			Vector2D velocity = node.physicsComponent.getVelocity();

			// Skip if not moving
			if (velocity.magnitudeSquared() < MIN_MOVEMENT_THRESHOLD) {
				return;
			}

			// Calculate displacement based on current velocity and delta time
			float deltaTime = (float) Time.getDeltaTime();
			Vector2D displacement = velocity.scale(deltaTime);

			// Entity has a collider - use collision-aware movement
			if (node.getEntity().hasComponent(ColliderComponent.class) && collisionService.isPresent()) {
				moveWithCollision(node, currentPos, displacement);
			} else {
				// No collider - move directly
				Vector2D newPos = currentPos.add(displacement);
				node.transform.setPosition(newPos);

				if (DEBUG_PHYSICS) {
					System.out.printf("Physics: Moving from (%.2f, %.2f) to (%.2f, %.2f)%n",
						currentPos.x(), currentPos.y(), newPos.x(), newPos.y());
				}
			}
		});
	}

	/**
	 * Applies friction to the physics node's velocity.
	 */
	private void applyFriction(PhysicsNode node) {
		Vector2D velocity = node.physicsComponent.getVelocity();
		if (velocity.magnitudeSquared() < MIN_MOVEMENT_THRESHOLD) {
			return;
		}

		// Calculate friction based on fixed delta time for consistency
		Vector2D friction = velocity.scale(node.physicsComponent.getFriction() *
			(float)Time.getFixedDeltaTime());

		// Apply friction
		Vector2D newVelocity = velocity.subtract(friction);

		// Reset very small velocities to prevent jitter from tiny movements
		if (newVelocity.magnitudeSquared() < VELOCITY_RESET_THRESHOLD) {
			newVelocity = new Vector2D(0, 0);
		}

		node.physicsComponent.setVelocity(newVelocity);
	}

	/**
	 * Moves an entity with collision detection.
	 * Splits movement into components to slide along obstacles.
	 */
	private void moveWithCollision(PhysicsNode node, Vector2D currentPos, Vector2D displacement) {
		ColliderComponent collider = node.getEntity().getComponent(ColliderComponent.class);

		// Try moving on X axis
		Vector2D xMovement = new Vector2D(displacement.x(), 0);
		boolean canMoveX = isAxisMovementValid(collider, currentPos, xMovement);

		// Try moving on Y axis
		Vector2D yMovement = new Vector2D(0, displacement.y());
		boolean canMoveY = isAxisMovementValid(collider, currentPos, yMovement);

		// Calculate new position based on allowed movement
		Vector2D newPos = currentPos;

		// Apply X movement if valid
		if (canMoveX) {
			newPos = newPos.add(xMovement);
		} else if (Math.abs(displacement.x()) > 0.01f) {
			// If X movement blocked, kill X velocity to prevent buildup
			Vector2D velocity = node.physicsComponent.getVelocity();
			node.physicsComponent.setVelocity(new Vector2D(0, velocity.y()));
		}

		// Apply Y movement if valid
		if (canMoveY) {
			newPos = newPos.add(yMovement);
		} else if (Math.abs(displacement.y()) > 0.01f) {
			// If Y movement blocked, kill Y velocity to prevent buildup
			Vector2D velocity = node.physicsComponent.getVelocity();
			node.physicsComponent.setVelocity(new Vector2D(velocity.x(), 0));
		}

		// Update position
		node.transform.setPosition(newPos);

		if (DEBUG_PHYSICS && !newPos.equals(currentPos)) {
			System.out.printf("Physics: Moving with collision from (%.2f, %.2f) to (%.2f, %.2f)%n",
				currentPos.x(), currentPos.y(), newPos.x(), newPos.y());
		}
	}

	/**
	 * Tests if movement along a single axis is valid.
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