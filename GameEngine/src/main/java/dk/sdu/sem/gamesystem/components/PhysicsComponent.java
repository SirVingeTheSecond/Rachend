package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;

public class PhysicsComponent implements IComponent {
	private Vector2D velocity = new Vector2D(0, 0);
	private float friction;
	private float mass;

	// Accumulated forces and impulses
	private Vector2D accumulatedForce = new Vector2D(0, 0);
	private Vector2D accumulatedImpulse = new Vector2D(0, 0);

	// Sleep state management
	private boolean isSleeping = false;
	private static final float SLEEP_THRESHOLD = 0.5f;
	private static final float SLEEP_TIME_THRESHOLD = 0.5f;
	private float timeAtRest = 0f;

	// Movement thresholds
	private static final float MIN_VELOCITY = 0.01f;
	private static final float MAX_VELOCITY = 2000.0f;

	public PhysicsComponent(float friction, float mass) {
		this.friction = friction;
		this.mass = Math.max(0.0001f, mass);
	}

	public Vector2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2D velocity) {
		// Enforce velocity limits
		float speed = velocity.magnitude();
		if (speed > MAX_VELOCITY) {
			velocity = velocity.scale(MAX_VELOCITY / speed);
		} else if (speed < MIN_VELOCITY) {
			velocity = new Vector2D(0, 0);
		}

		// Wake if velocity changes significantly
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

	public float getEffectiveMass() {
		return isSleeping ? mass * 10.0f : mass;
	}

	public void addForce(Vector2D force) {
		if (force == null) return;
		wakeDynamicBody();
		accumulatedForce = accumulatedForce.add(force);
	}

	public void addImpulse(Vector2D impulse) {
		if (impulse == null) return;
		wakeDynamicBody();
		accumulatedImpulse = accumulatedImpulse.add(impulse);
	}

	public void applyAccumulatedForcesAndImpulses() {
		// Apply forces
		if (accumulatedForce.magnitudeSquared() > 0.0001f) {
			Vector2D acceleration = accumulatedForce.scale(1.0f / mass);
			Vector2D deltaVelocity = acceleration.scale((float)Time.getFixedDeltaTime());
			velocity = velocity.add(deltaVelocity);
			accumulatedForce = new Vector2D(0, 0);
		}

		// Apply impulses
		if (accumulatedImpulse.magnitudeSquared() > 0.0001f) {
			Vector2D deltaVelocity = accumulatedImpulse.scale(1.0f / mass);
			velocity = velocity.add(deltaVelocity);
			accumulatedImpulse = new Vector2D(0, 0);
		}

		// Enforce velocity limits
		float speed = velocity.magnitude();
		if (speed > MAX_VELOCITY) {
			velocity = velocity.scale(MAX_VELOCITY / speed);
		} else if (speed < MIN_VELOCITY) {
			velocity = new Vector2D(0, 0);
		}
	}

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

	public void wakeDynamicBody() {
		isSleeping = false;
		timeAtRest = 0f;
	}

	public boolean isSleeping() {
		return isSleeping;
	}
}