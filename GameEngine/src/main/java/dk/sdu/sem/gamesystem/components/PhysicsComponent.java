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

	// Sleep Zzz...
	private boolean isSleeping = false;
	private static final float SLEEP_THRESHOLD = 0.5f;  // Velocity magnitude to consider "at rest"
	private static final float SLEEP_TIME_THRESHOLD = 0.5f;  // Time in seconds to be "at rest" before sleeping
	private float timeAtRest = 0f;

	/**
	 * Creates a physics component with default mass of 1
	 */
	public PhysicsComponent(float friction) {
		this(friction, 10f);
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
		// If velocity is changing significantly, wake the body
		if (this.velocity.subtract(velocity).magnitudeSquared() > SLEEP_THRESHOLD * SLEEP_THRESHOLD) {
			wakeDynamicBody();
		}
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

	/**
	 * Gets an effective mass based on sleep state.
	 * Sleeping bodies have much higher effective mass.
	 */
	public float getEffectiveMass() {
		return isSleeping ? mass * 10.0f : mass;
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
		wakeDynamicBody();
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
		wakeDynamicBody();
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
			Vector2D deltaVelocity = acceleration.scale(deltaTime);

			// Update velocity
			velocity = velocity.add(deltaVelocity);

			// Reset accumulated force
			accumulatedForce = new Vector2D(0, 0);
		}

		// Apply accumulated impulses (I = m∆v => ∆v = I/m)
		if (accumulatedImpulse.magnitudeSquared() > 0.0001f) {
			// Calculate velocity change
			Vector2D deltaVelocity = accumulatedImpulse.scale(1.0f / mass);

			// Update velocity immediately
			velocity = velocity.add(deltaVelocity);

			// Reset accumulated impulse
			accumulatedImpulse = new Vector2D(0, 0);
		}
	}

	/**
	 * Updates the sleep state of this physics component.
	 * Called by PhysicsSystem during fixed update.
	 * @param deltaTime Time since last fixed update
	 */
	public void updateSleepState(float deltaTime) {
		if (velocity.magnitudeSquared() < SLEEP_THRESHOLD * SLEEP_THRESHOLD) {
			timeAtRest += deltaTime;
			if (timeAtRest > SLEEP_TIME_THRESHOLD && !isSleeping) {
				isSleeping = true;
			}
		} else {
			wakeDynamicBody();
		}
	}

	/**
	 * Wakes the body from sleep state.
	 */
	public void wakeDynamicBody() {
		isSleeping = false;
		timeAtRest = 0f;
	}

	/**
	 * Checks if this physics body is sleeping.
	 */
	public boolean isSleeping() {
		return isSleeping;
	}
}