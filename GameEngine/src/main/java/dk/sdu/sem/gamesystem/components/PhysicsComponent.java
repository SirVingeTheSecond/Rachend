package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;

public class PhysicsComponent implements IComponent {
	private Vector2D velocity = new Vector2D(0, 0);
	private float friction;
	private float mass;

	// For forces to be accumulated in FixedUpdate
	private Vector2D accumulatedForce = new Vector2D(0, 0);
	private Vector2D accumulatedImpulse = new Vector2D(0, 0);

	/**
	 * Creates a physics component with default mass of 1
	 */
	public PhysicsComponent(float friction) {
		this(friction, 1.0f);
	}

	/**
	 * Creates a physics component with specified mass
	 */
	public PhysicsComponent(float friction, float mass) {
		this.friction = friction;
		this.mass = Math.max(0.0001f, mass);
	}

	public Vector2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2D velocity) {
		this.velocity = velocity;
	}

	public float getFriction() {
		return friction;
	}

	public void setFriction(float friction) {
		this.friction = friction;
	}

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = Math.max(0.0001f, mass);
	}

	/**
	 * Adds a force to this physics component
	 * Force is applied over time
	 *
	 * @param force The force vector to apply
	 */
	public void addForce(Vector2D force) {
		if (force == null) return;
		accumulatedForce = accumulatedForce.add(force);
	}

	/**
	 * Adds a force to this physics component
	 *
	 * @param forceX X component of the force
	 * @param forceY Y component of the force
	 */
	public void addForce(float forceX, float forceY) {
		addForce(new Vector2D(forceX, forceY));
	}

	/**
	 * Adds an impulse to this physics component
	 * Impulse is applied instantaneously (velocity changes immediately)
	 *
	 * @param impulse The impulse vector to apply
	 */
	public void addImpulse(Vector2D impulse) {
		if (impulse == null) return;
		accumulatedImpulse = accumulatedImpulse.add(impulse);
	}

	/**
	 * Adds an impulse to this physics component
	 *
	 * @param impulseX X component of the impulse
	 * @param impulseY Y component of the impulse
	 */
	public void addImpulse(float impulseX, float impulseY) {
		addImpulse(new Vector2D(impulseX, impulseY));
	}

	/**
	 * Called by the physics system to apply accumulated forces and impulses
	 * This should be called in PhysicsSystem's fixedUpdate method
	 */
	public void applyAccumulatedForcesAndImpulses() {
		// Apply accumulated forces (F = ma => a = F/m)
		if (accumulatedForce.magnitudeSquared() > 0.0001f) {
			// Calculate acceleration
			Vector2D acceleration = accumulatedForce.scale(1.0f / mass);

			// Apply acceleration over time
			float deltaTime = (float) Time.getFixedDeltaTime();
			Vector2D velocityChange = acceleration.scale(deltaTime);

			// Update velocity
			velocity = velocity.add(velocityChange);

			// Reset accumulated force
			accumulatedForce = new Vector2D(0, 0);
		}

		// Apply accumulated impulses (I = m∆v => ∆v = I/m)
		if (accumulatedImpulse.magnitudeSquared() > 0.0001f) {
			// Calculate velocity change
			Vector2D velocityChange = accumulatedImpulse.scale(1.0f / mass);

			// Update velocity immediately
			velocity = velocity.add(velocityChange);

			// Reset accumulated impulse
			accumulatedImpulse = new Vector2D(0, 0);
		}
	}
}