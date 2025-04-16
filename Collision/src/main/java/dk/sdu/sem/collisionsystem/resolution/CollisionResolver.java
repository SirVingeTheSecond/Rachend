package dk.sdu.sem.collisionsystem.resolution;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collisionsystem.CollisionPair;
import dk.sdu.sem.collisionsystem.ContactPoint;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.ServiceLocator;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.commonsystem.Entity;

import java.util.Set;

/**
 * Handles resolving collisions between entities with physics components.
 */
public class CollisionResolver {
	private static final float CORRECTION_PERCENT = 0.4f; // Penetration correction percentage (0.2-0.8 seems pretty valid)
	private static final float CORRECTION_SLOP = 0.01f;   // Small penetration tolerance to avoid jitter
	private static final float RESTITUTION = 0.2f;        // "Bounciness" coefficient

	/**
	 * Resolves collisions between entities.
	 *
	 * @param collisions Set of collision pairs to resolve
	 */
	public void resolveCollisions(Set<CollisionPair> collisions) {
		for (CollisionPair pair : collisions) {
			// Skip if this is a trigger collision
			if (pair.isTrigger()) {
				continue;
			}

			// Get entities
			Entity entityA = pair.getEntityA();
			Entity entityB = pair.getEntityB();

			// Get physics components if they exist
			PhysicsComponent physicsA = entityA.getComponent(PhysicsComponent.class);
			PhysicsComponent physicsB = entityB.getComponent(PhysicsComponent.class);

			// Skip if both entities don't have physics
			if (physicsA == null && physicsB == null) {
				continue;
			}

			ContactPoint contact = pair.getContact();
			if (contact == null) {
				continue;
			}

			// Apply collision response based on available physics components
			if (physicsA != null && physicsB != null) {
				// Both entities have physics - resolve with momentum conservation
				resolveRigidBodyCollision(
					entityA, physicsA,
					entityB, physicsB,
					contact.getNormal(),
					contact.getPenetrationDepth()
				);
			}
			else if (physicsA != null) {
				// Only entity A has physics - treat B as immovable
				resolveStaticCollision(
					entityA, physicsA,
					contact.getNormal(),
					contact.getPenetrationDepth()
				);
			}
			else if (physicsB != null) {
				// Only entity B has physics - treat A as immovable
				// Flip normal direction since we're resolving from B's perspective
				resolveStaticCollision(
					entityB, physicsB,
					contact.getNormal().scale(-1),
					contact.getPenetrationDepth()
				);
			}
		}
	}

	/**
	 * Resolves collision between two physics bodies.
	 * Uses both impulse resolution and direct position correction.
	 */
	private void resolveRigidBodyCollision(
		Entity entityA, PhysicsComponent physicsA,
		Entity entityB, PhysicsComponent physicsB,
		Vector2D normal,
		float penetrationDepth) {

		// Get velocities
		Vector2D velocityA = physicsA.getVelocity();
		Vector2D velocityB = physicsB.getVelocity();

		// Calculate relative velocity
		Vector2D relativeVelocity = velocityB.subtract(velocityA);

		// Calculate relative velocity along the normal
		float normalVelocity = relativeVelocity.x() * normal.x() +
			relativeVelocity.y() * normal.y();

		// Do not resolve if objects are separating
		if (normalVelocity > 0) {
			return;
		}

		// Calculate restitution (bounciness)
		float e = RESTITUTION;

		// Use effective mass which accounts for sleep state
		float effectiveMassA = physicsA.getEffectiveMass();
		float effectiveMassB = physicsB.getEffectiveMass();

		// Calculate impulse scalar
		float j = -(1 + e) * normalVelocity;
		j /= (1 / effectiveMassA) + (1 / effectiveMassB);

		// Apply impulse
		Vector2D impulse = normal.scale(j);
		physicsA.addImpulse(impulse.scale(-1));
		physicsB.addImpulse(impulse);

		// Direct position correction to prevent sinking (Option 1)
		if (penetrationDepth > CORRECTION_SLOP) {
			TransformComponent transformA = entityA.getComponent(TransformComponent.class);
			TransformComponent transformB = entityB.getComponent(TransformComponent.class);

			if (transformA != null && transformB != null) {
				float totalMass = effectiveMassA + effectiveMassB;

				// Calculate how much each object should move based on mass ratio
				float ratioA = effectiveMassB / totalMass;
				float ratioB = effectiveMassA / totalMass;

				// Calculate correction vectors
				Vector2D correction = normal.scale(CORRECTION_PERCENT * penetrationDepth);
				Vector2D correctionA = correction.scale(ratioA);
				Vector2D correctionB = correction.scale(ratioB);

				// Calculate proposed new positions
				Vector2D proposedPositionA = transformA.getPosition().subtract(correctionA);
				Vector2D proposedPositionB = transformB.getPosition().add(correctionB);

				// Validate positions against tilemaps
				boolean positionAValid = true;
				boolean positionBValid = true;

				// Only check if entity has a collider
				if (entityA.hasComponent(ColliderComponent.class)) {
					ColliderComponent colliderA = entityA.getComponent(ColliderComponent.class);
					// Get collision service
					ICollisionSPI collisionService = ServiceLocator.getCollisionSystem();
					if (collisionService != null) {
						positionAValid = collisionService.isPositionValid(colliderA, proposedPositionA);
					}
				}

				if (entityB.hasComponent(ColliderComponent.class)) {
					ColliderComponent colliderB = entityB.getComponent(ColliderComponent.class);
					ICollisionSPI collisionService = ServiceLocator.getCollisionSystem();
					if (collisionService != null) {
						positionBValid = collisionService.isPositionValid(colliderB, proposedPositionB);
					}
				}

				// Only apply valid positions
				if (positionAValid) {
					transformA.setPosition(proposedPositionA);
				}

				if (positionBValid) {
					transformB.setPosition(proposedPositionB);
				}
			}
		}
	}

	/**
	 * Resolves collision with a static (immovable) object.
	 * Uses both impulse resolution and direct position correction.
	 */
	private void resolveStaticCollision(
		Entity entity, PhysicsComponent physics,
		Vector2D normal,
		float penetrationDepth) {

		// Get velocity
		Vector2D velocity = physics.getVelocity();

		// Calculate velocity along the normal
		float normalVelocity = velocity.x() * normal.x() +
			velocity.y() * normal.y();

		// Do not resolve if objects are separating
		if (normalVelocity > 0) {
			return;
		}

		// Calculate restitution (bounciness)
		float e = RESTITUTION;

		// Calculate impulse scalar
		float j = -(1 + e) * normalVelocity;

		// Apply impulse
		Vector2D impulse = normal.scale(j);
		physics.setVelocity(velocity.add(impulse));

		// Direct position correction to prevent sinking (Option 1)
		if (penetrationDepth > CORRECTION_SLOP) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			if (transform != null) {
				// Calculate proposed position with correction
				Vector2D correction = normal.scale(CORRECTION_PERCENT * penetrationDepth);
				Vector2D proposedPosition = transform.getPosition().add(correction);

				// Validate if entity has a collider
				boolean positionValid = true;
				if (entity.hasComponent(ColliderComponent.class)) {
					ColliderComponent collider = entity.getComponent(ColliderComponent.class);
					ICollisionSPI collisionService = ServiceLocator.getCollisionSystem();
					if (collisionService != null) {
						positionValid = collisionService.isPositionValid(collider, proposedPosition);
					}
				}

				// Only apply if position is valid
				if (positionValid) {
					transform.setPosition(proposedPosition);
				}
			}
		}
	}
}