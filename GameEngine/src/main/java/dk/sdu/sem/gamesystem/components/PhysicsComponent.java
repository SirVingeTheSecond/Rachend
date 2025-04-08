package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;

public class PhysicsComponent implements IComponent {
	private Vector2D velocity = new Vector2D(0, 0);
	private float mass;
	private float friction;

	public PhysicsComponent(float friction) {
		this.friction = friction;
		this.mass = 1;
	}

	public PhysicsComponent(float friction, float mass) {
		this.friction = friction;
		this.mass = mass;
	}

	public void addForce(Vector2D force) {
		Vector2D acceleration = force.scale(1 / mass);
		Vector2D deltaVelocity = acceleration.scale((float)Time.getFixedDeltaTime());
		velocity = velocity.add(deltaVelocity);
	}

	public void addImpulse(Vector2D impulse) {
		Vector2D acceleration = impulse.scale(1 / mass);
		velocity = velocity.add(acceleration);
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
		this.mass = mass;
	}
}
