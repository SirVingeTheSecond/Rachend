package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Vector2D;

public final class CircleShape implements ICollisionShape {
	private final Vector2D center;
	private final float radius;

	public CircleShape(Vector2D center, float radius) {
		this.center = center;
		this.radius = radius;
	}

	public Vector2D getCenter() {
		return center;
	}

	public float getRadius() {
		return radius;
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		if (other instanceof CircleShape) {
			CircleShape circle = (CircleShape) other;
			float distance = center.distance(circle.getCenter());
			return distance < (this.radius + circle.getRadius());
		}
		// Box could be added here
		return false;
	}
}
