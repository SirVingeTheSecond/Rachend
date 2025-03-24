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
 * System responsible for physics simulation.
 */
public class PhysicsSystem implements IFixedUpdate, IUpdate {
	private final Optional<ICollisionSPI> collisionService;

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
		// If the collision service exists, collision handling is done in the CollisionSystem
		// This just updates positions for entities without colliders or when collision module is absent

		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D currentPos = node.transform.getPosition();
			Vector2D velocity = node.physicsComponent.getVelocity();

			// Skip if not moving
			if (velocity.magnitudeSquared() < 0.001f) {
				return;
			}

			Vector2D proposedPos = currentPos.add(velocity.scale((float) Time.getDeltaTime()));
			
			boolean canMove = true;
			if (collisionService.isPresent() && node.getEntity().hasComponent(ColliderComponent.class)) {
				ColliderComponent collider = node.getEntity().getComponent(ColliderComponent.class);

				// Create cache key
				Pair<ColliderComponent, Vector2D> cacheKey = Pair.of(collider, proposedPos);

				// Check cache first
				Boolean cached = positionValidCache.get(cacheKey);
				if (cached != null) {
					canMove = cached;
				} else {
					// Calculate and cache result
					canMove = collisionService.get().isPositionValid(collider, proposedPos);
					positionValidCache.put(cacheKey, canMove);
				}
			}

			// Only update position if movement is valid
			if (canMove) {
				node.transform.setPosition(proposedPos);
			} else {
				// Simple collision response - zero out velocity
				node.physicsComponent.setVelocity(new Vector2D(0, 0));
			}
		});
	}
}