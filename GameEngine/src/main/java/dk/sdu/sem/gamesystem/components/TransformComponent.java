package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.Vector2D;

public class TransformComponent {
	private Vector2D position;
	private float rotation;
	private Vector2D scale;

	public TransformComponent(Vector2D position, float rotation, Vector2D scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}

	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public Vector2D getScale() {
		return scale;
	}

	public void setScale(Vector2D scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		return "TransformComponent [position=" + position + ", rotation=" + rotation + ", scale=" + scale + "]";
	}
}