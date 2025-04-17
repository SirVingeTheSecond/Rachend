package dk.sdu.sem.commonsystem;

public class TransformComponent implements IComponent {
	private Vector2D position;
	private float rotation;
	private Vector2D scale;

	public TransformComponent(Vector2D position, float rotation, Vector2D scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}

	public TransformComponent(Vector2D position, float rotation) {
		this.position = position;
		this.rotation = rotation;
		this.scale = new Vector2D(1, 1);
	}

	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}

	/**
	 * Get Rotation in Radians
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * Set Rotation in Radians
	 */
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