package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

public class PhysicsComponent implements IComponent {
	private Vector2D velocity = new Vector2D(0, 0);
	private float friction;

	public PhysicsComponent(float friction) {
		this.friction = friction;
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
}
