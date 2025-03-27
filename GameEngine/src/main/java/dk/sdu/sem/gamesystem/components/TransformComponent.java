package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Vector2D;

public class TransformComponent implements IComponent {
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

	public void translate(Vector2D translation) {
		this.position = this.position.add(translation);
	}

	public Vector2D forward() {
		return new Vector2D((float) Math.cos(rotation), (float) Math.sin(rotation));
	}

	public TransformComponent copy() {
		return new TransformComponent(position, rotation, scale);
	}
}