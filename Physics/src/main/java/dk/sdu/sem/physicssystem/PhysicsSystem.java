package dk.sdu.sem.physicssystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.CollisionOptions;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * System responsible for physics simulation.
 */
public class PhysicsSystem implements IFixedUpdate, IUpdate {
	private final Optional<ICollisionSPI> collisionService; // This might not be correct use of Optional

	private static final boolean DEBUG_PHYSICS = false;
	private static final float MIN_MOVEMENT_THRESHOLD = 0.001f;
	private static final float VELOCITY_RESET_THRESHOLD = 0.01f;

	// Cache valid positions
	// Potentially optimize performance by avoiding redundant collision checks
	// when validating the same position multiple times within a frame.
	private final Map<Pair<ColliderComponent, Vector2D>, Boolean> positionValidCache = new HashMap<>();

	public PhysicsSystem() {
		collisionService = ServiceLoader.load(ICollisionSPI.class).findFirst();

		if (collisionService.isPresent()) {
			System.out.println("Collision service obtained through ServiceLoader");
		} else {
			System.out.println("No collision service available - physics will not check for collisions");
		}
	}

	@Override
	public void fixedUpdate() {
		positionValidCache.clear();

		// Update sleep states for all physics components
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			node.physicsComponent.updateSleepState((float)Time.getFixedDeltaTime());
		});

		// Apply accumulated forces and impulses to all physics components
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			node.physicsComponent.applyAccumulatedForcesAndImpulses();
		});

		// Apply friction to all physics objects
		NodeManager.active().getNodes(PhysicsNode.class).forEach(this::applyFriction);
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

			// Entity has a collider
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
	 * Allows dynamic entities to overlap but prevents static collisions.
	 */
	private void moveWithCollision(PhysicsNode node, Vector2D currentPos, Vector2D displacement) {
		Entity entity = node.getEntity();
		ColliderComponent collider = entity.getComponent(ColliderComponent.class);

		// Create options that prevent static collisions but allow dynamic overlaps
		CollisionOptions options = CollisionOptions.preventStaticOnly(true);

		// Check if we can move directly to the target position
		Vector2D targetPos = currentPos.add(displacement);
		if (collisionService.get().isPositionValid(entity, targetPos, options)) {
			// We can move directly
			node.transform.setPosition(targetPos);
			return;
		}
		//Only trigger events for the original position check
		options.setTriggerEvents(false);

		// If direct movement isn't possible, try axis-separated movement
		boolean movedX = false;
		boolean movedY = false;

		// Try X-axis movement
		if (Math.abs(displacement.x()) > 0.001f) {
			Vector2D xPos = currentPos.add(new Vector2D(displacement.x(), 0));
			if (collisionService.get().isPositionValid(entity, xPos, options)) {
				currentPos = xPos;
				movedX = true;
			} else {
				// X movement blocked, zero velocity to prevent buildup
				Vector2D velocity = node.physicsComponent.getVelocity();
				node.physicsComponent.setVelocity(new Vector2D(0, velocity.y()));
			}
		}

		// Try Y-axis movement
		if (Math.abs(displacement.y()) > 0.001f) {
			Vector2D yPos = currentPos.add(new Vector2D(0, displacement.y()));
			if (collisionService.get().isPositionValid(entity, yPos, options)) {
				currentPos = yPos;
				movedY = true;
			} else {
				// Y movement blocked, zero velocity to prevent buildup
				Vector2D velocity = node.physicsComponent.getVelocity();
				node.physicsComponent.setVelocity(new Vector2D(velocity.x(), 0));
			}
		}

		// Update position if we moved in at least one direction
		if (movedX || movedY) {
			node.transform.setPosition(currentPos);
		}

		if (DEBUG_PHYSICS && (!currentPos.equals(entity.getComponent(TransformComponent.class).getPosition()))) {
			System.out.printf("Physics: Moving with collision from (%.2f, %.2f) to (%.2f, %.2f)%n",
				entity.getComponent(TransformComponent.class).getPosition().x(),
				entity.getComponent(TransformComponent.class).getPosition().y(),
				currentPos.x(), currentPos.y());
		}
	}
}