package dk.sdu.sem.collisionsystem.resolution;

import dk.sdu.sem.collisionsystem.CollisionPair;
import dk.sdu.sem.collisionsystem.ContactPoint;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

/**
 * Handles resolving collisions between entities with physics components.
 */
public class CollisionResolver {
	private static final float CORRECTION_FACTOR = 0.2f; // Penetration correction scale
	private static final float RESTITUTION = 0.2f;       // "Bounciness" coefficient

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

			// Get physics components if they exist
			PhysicsComponent physicsA = pair.getEntityA().getComponent(PhysicsComponent.class);
			PhysicsComponent physicsB = pair.getEntityB().getComponent(PhysicsComponent.class);

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
					physicsA,
					physicsB,
					contact.getNormal(),
					contact.getPenetrationDepth()
				);
			}
			else if (physicsA != null) {
				// Only entity A has physics - treat B as immovable
				resolveStaticCollision(
					physicsA,
					contact.getNormal(),
					contact.getPenetrationDepth()
				);
			}
			else if (physicsB != null) {
				// Only entity B has physics - treat A as immovable
				// Flip normal direction since we're resolving from B's perspective
				resolveStaticCollision(
					physicsB,
					contact.getNormal().scale(-1),
					contact.getPenetrationDepth()
				);
			}
		}
	}

	/**
	 * Resolves collision between two physics bodies.
	 */
	private void resolveRigidBodyCollision(
		PhysicsComponent physicsA,
		PhysicsComponent physicsB,
		Vector2D normal,
		float penetrationDepth) {

		// Get velocities
		Vector2D velocityA = physicsA.getVelocity();
		Vector2D velocityB = physicsB.getVelocity();

		// Calculate relative velocity
		Vector2D relativeVelocity = velocityB.subtract(velocityA);

		// Calculate relative velocity along the normal
		float normalVelocity = relativeVelocity.getX() * normal.getX() +
			relativeVelocity.getY() * normal.getY();

		// Do not resolve if objects are separating
		if (normalVelocity > 0) {
			return;
		}

		// Calculate restitution (bounciness)
		float e = RESTITUTION;

		// Calculate impulse scalar
		float j = -(1 + e) * normalVelocity;
		j /= 2.0f; // Assuming equal mass for simplicity

		// Apply impulse
		Vector2D impulse = normal.scale(j);
		physicsA.setVelocity(velocityA.subtract(impulse));
		physicsB.setVelocity(velocityB.add(impulse));

		// Positional correction to prevent sinking
		Vector2D correction = normal.scale(penetrationDepth * CORRECTION_FACTOR);
		physicsA.setVelocity(physicsA.getVelocity().subtract(correction));
		physicsB.setVelocity(physicsB.getVelocity().add(correction));
	}

	/**
	 * Resolves collision with a static (immovable) object.
	 */
	private void resolveStaticCollision(
		PhysicsComponent physics,
		Vector2D normal,
		float penetrationDepth) {

		// Get velocity
		Vector2D velocity = physics.getVelocity();

		// Calculate velocity along the normal
		float normalVelocity = velocity.getX() * normal.getX() +
			velocity.getY() * normal.getY();

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

		// Positional correction to prevent sinking
		Vector2D correction = normal.scale(penetrationDepth * CORRECTION_FACTOR);
		physics.setVelocity(physics.getVelocity().add(correction));
	}
}