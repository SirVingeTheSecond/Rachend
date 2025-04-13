package dk.sdu.sem.collisionsystem.resolution;

import dk.sdu.sem.collision.CollisionPair;
import dk.sdu.sem.collision.ContactPoint;
import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.narrowphase.solvers.ShapeSolverFactory;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Entity;

import java.util.ServiceLoader;
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
					contact.getSeparation()
				);
			}
			else if (physicsA != null) {
				// Only entity A has physics - treat B as immovable
				resolveStaticCollision(
					entityA, physicsA,
					contact.getNormal(),
					contact.getSeparation()
				);
			}
			else if (physicsB != null) {
				// Only entity B has physics - treat A as immovable
				// Flip normal direction since we're resolving from B's perspective
				resolveStaticCollision(
					entityB, physicsB,
					contact.getNormal().scale(-1),
					contact.getSeparation()
				);
			}
		}
	}

	/**
	 * Retrieves the current ICollisionSPI implementation using ServiceLoader.
	 */
	private ICollisionSPI getCollisionSPI() {
		return ServiceLoader.load(ICollisionSPI.class)
			.findFirst()
			.orElseThrow(() ->
				new IllegalStateException("No implementation found for ICollisionSPI")
			);
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

		// Retrieve velocities
		Vector2D velocityA = physicsA.getVelocity();
		Vector2D velocityB = physicsB.getVelocity();

		// Calculate relative velocity and its component along the collision normal
		Vector2D relativeVelocity = velocityB.subtract(velocityA);
		float normalVelocity = relativeVelocity.x() * normal.x() +
			relativeVelocity.y() * normal.y();

		// Do not resolve collision if objects are moving apart
		if (normalVelocity > 0) {
			return;
		}

		// Calculate restitution and effective masses
		float e = RESTITUTION;
		float effectiveMassA = physicsA.getEffectiveMass();
		float effectiveMassB = physicsB.getEffectiveMass();

		// Calculate impulse scalar
		float j = -(1 + e) * normalVelocity / ((1 / effectiveMassA) + (1 / effectiveMassB));

		// Apply impulses
		Vector2D impulse = normal.scale(j);
		physicsA.addImpulse(impulse.scale(-1));
		physicsB.addImpulse(impulse);

		// Correct positions if penetration is significant
		if (penetrationDepth > CORRECTION_SLOP) {
			TransformComponent transformA = entityA.getComponent(TransformComponent.class);
			TransformComponent transformB = entityB.getComponent(TransformComponent.class);

			if (transformA != null && transformB != null) {
				float totalMass = effectiveMassA + effectiveMassB;
				float ratioA = effectiveMassB / totalMass;
				float ratioB = effectiveMassA / totalMass;

				Vector2D correction = normal.scale(CORRECTION_PERCENT * penetrationDepth);
				Vector2D correctionA = correction.scale(ratioA);
				Vector2D correctionB = correction.scale(ratioB);

				Vector2D proposedPositionA = transformA.getPosition().subtract(correctionA);
				Vector2D proposedPositionB = transformB.getPosition().add(correctionB);

				ICollisionSPI collisionService = getCollisionSPI();

				boolean positionAValid = true;
				boolean positionBValid = true;

				if (entityA.hasComponent(ColliderComponent.class)) {
					ColliderComponent colliderA = entityA.getComponent(ColliderComponent.class);
					positionAValid = collisionService.isPositionValid(colliderA, proposedPositionA);
				}

				if (entityB.hasComponent(ColliderComponent.class)) {
					ColliderComponent colliderB = entityB.getComponent(ColliderComponent.class);
					positionBValid = collisionService.isPositionValid(colliderB, proposedPositionB);
				}

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
		Entity entity,
		PhysicsComponent physics,
		Vector2D normal,
		float penetrationDepth) {

		// Retrieve the current velocity.
		Vector2D velocity = physics.getVelocity();

		// Calculate the component of velocity along the collision normal.
		float normalVelocity = velocity.x() * normal.x() + velocity.y() * normal.y();

		// Do not resolve collision if the object is moving away from the static object.
		if (normalVelocity > 0) {
			return;
		}

		// Calculate restitution (bounciness) and impulse scalar.
		float e = RESTITUTION;
		float j = -(1 + e) * normalVelocity;

		// Apply impulse to update the velocity.
		Vector2D impulse = normal.scale(j);
		physics.setVelocity(velocity.add(impulse));

		// Use direct position correction to prevent sinking if penetration is significant.
		if (penetrationDepth > CORRECTION_SLOP) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			if (transform != null) {
				// Calculate the proposed correction position.
				Vector2D correction = normal.scale(CORRECTION_PERCENT * penetrationDepth);
				Vector2D proposedPosition = transform.getPosition().add(correction);

				boolean positionValid = true;
				// Validate the new position if the entity has a collider.
				if (entity.hasComponent(ColliderComponent.class)) {
					ColliderComponent collider = entity.getComponent(ColliderComponent.class);
					ICollisionSPI collisionService = getCollisionSPI();
					positionValid = collisionService.isPositionValid(collider, proposedPosition);
				}

				// Apply the correction only if the new position is valid.
				if (positionValid) {
					transform.setPosition(proposedPosition);
				}
			}
		}
	}
}