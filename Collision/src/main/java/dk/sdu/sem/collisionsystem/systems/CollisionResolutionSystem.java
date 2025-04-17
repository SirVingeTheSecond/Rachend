package dk.sdu.sem.collisionsystem.systems;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.CollisionPair;
import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collisionsystem.state.CollisionState;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.ServiceLoader;
import java.util.Set;

public class CollisionResolutionSystem {
	// Resolution parameters
	private static final float CORRECTION_PERCENT = 0.4f;    // Penetration correction percentage (0.2-0.8 recommended)
	private static final float CORRECTION_SLOP = 0.01f;      // Small penetration tolerance
	private static final float RESTITUTION = 0.2f;          // "Bounciness" coefficient
	private static final float SEPARATION_THRESHOLD = 0.1f;  // Velocity threshold for considering objects as separating

	// Collision state
	private final CollisionState collisionState;

	public CollisionResolutionSystem(CollisionState collisionState) {
		this.collisionState = collisionState;
	}

	public void process() {
		Set<CollisionPair> collisions = collisionState.getCurrentCollisions();

		for (CollisionPair pair : collisions) {
			// Skip trigger collisions
			if (pair.isTrigger()) {
				continue;
			}

			Entity entityA = pair.getEntityA();
			Entity entityB = pair.getEntityB();

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
				resolveRigidBodyCollision(
					entityA, physicsA,
					entityB, physicsB,
					contact.getNormal(),
					contact.getSeparation()
				);
			} else if (physicsA != null) {
				resolveStaticCollision(
					entityA, physicsA,
					contact.getNormal(),
					contact.getSeparation()
				);
			} else if (physicsB != null) {
				resolveStaticCollision(
					entityB, physicsB,
					contact.getNormal().scale(-1),
					contact.getSeparation()
				);
			}
		}
	}

	private void resolveRigidBodyCollision(
		Entity entityA, PhysicsComponent physicsA,
		Entity entityB, PhysicsComponent physicsB,
		Vector2D normal, float penetrationDepth) {

		// Calculate relative velocity
		Vector2D relativeVelocity = physicsB.getVelocity().subtract(physicsA.getVelocity());
		float normalVelocity = relativeVelocity.dot(normal);

		// Skip if objects are separating
		if (normalVelocity > SEPARATION_THRESHOLD) {
			return;
		}

		// Calculate impulse using masses
		float effectiveMassA = physicsA.getEffectiveMass();
		float effectiveMassB = physicsB.getEffectiveMass();
		float totalMass = effectiveMassA + effectiveMassB;

		// Calculate impulse scalar
		float j = -(1 + RESTITUTION) * normalVelocity;
		j /= (1 / effectiveMassA) + (1 / effectiveMassB);

		// Apply impulse
		Vector2D impulse = normal.scale(j);
		physicsA.addImpulse(impulse.scale(-1));
		physicsB.addImpulse(impulse);

		// Apply position correction
		if (penetrationDepth > CORRECTION_SLOP) {
			applyPositionCorrection(
				entityA, entityB,
				normal, penetrationDepth,
				effectiveMassA, effectiveMassB
			);
		}
	}

	private void resolveStaticCollision(
		Entity entity, PhysicsComponent physics,
		Vector2D normal, float penetrationDepth) {

		Vector2D velocity = physics.getVelocity();
		float normalVelocity = velocity.dot(normal);

		// Skip if object is moving away
		if (normalVelocity > SEPARATION_THRESHOLD) {
			return;
		}

		// Calculate and apply impulse
		float j = -(1 + RESTITUTION) * normalVelocity;
		Vector2D impulse = normal.scale(j);
		physics.addImpulse(impulse);

		// Apply position correction
		if (penetrationDepth > CORRECTION_SLOP) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			if (transform != null) {
				Vector2D correction = normal.scale(CORRECTION_PERCENT * penetrationDepth);
				Vector2D proposedPosition = transform.getPosition().add(correction);

				if (validatePosition(entity, proposedPosition)) {
					transform.setPosition(proposedPosition);
				}
			}
		}
	}

	private void applyPositionCorrection(
		Entity entityA, Entity entityB,
		Vector2D normal, float penetrationDepth,
		float massA, float massB) {

		TransformComponent transformA = entityA.getComponent(TransformComponent.class);
		TransformComponent transformB = entityB.getComponent(TransformComponent.class);

		if (transformA == null || transformB == null) {
			return;
		}

		float totalMass = massA + massB;
		float ratioA = massB / totalMass;
		float ratioB = massA / totalMass;

		Vector2D correction = normal.scale(CORRECTION_PERCENT * penetrationDepth);
		Vector2D correctionA = correction.scale(ratioA);
		Vector2D correctionB = correction.scale(ratioB);

		Vector2D proposedPosA = transformA.getPosition().subtract(correctionA);
		Vector2D proposedPosB = transformB.getPosition().add(correctionB);

		if (validatePosition(entityA, proposedPosA)) {
			transformA.setPosition(proposedPosA);
		}
		if (validatePosition(entityB, proposedPosB)) {
			transformB.setPosition(proposedPosB);
		}
	}

	private boolean validatePosition(Entity entity, Vector2D proposedPosition) {
		if (entity.hasComponent(ColliderComponent.class)) {
			ColliderComponent collider = entity.getComponent(ColliderComponent.class);
			ICollisionSPI collisionService = ServiceLoader.load(ICollisionSPI.class)
				.findFirst()
				.orElse(null);

			if (collisionService != null) {
				return collisionService.isPositionValid(entity, proposedPosition);
			}
		}
		return true;
	}
}