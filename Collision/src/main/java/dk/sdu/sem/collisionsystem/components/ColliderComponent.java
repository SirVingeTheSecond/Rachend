package dk.sdu.sem.collisionsystem.components;

import dk.sdu.sem.collision.CircleShape;
import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.ICollisionShape;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.Entity;

import java.util.Objects;

public class ColliderComponent implements ICollider {

	private final Entity entity;
	private final ICollisionShape shape;

	/**
	 * Creates a circular collider for the specified entity.
	 *
	 * @param entity the entity this collider is attached to
	 * @param center the center point of the collider
	 * @param radius the radius of the collider
	 * @throws NullPointerException if entity or center is null
	 * @throws IllegalArgumentException if radius is negative or zero
	 */
	public ColliderComponent(Entity entity, Vector2D center, float radius) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		Objects.requireNonNull(center, "Center cannot be null");
		if (radius <= 0) {
			throw new IllegalArgumentException("Radius must be positive");
		}
		this.shape = new CircleShape(center, radius);
	}

	/**
	 * Creates a collider with a custom collision shape.
	 *
	 * @param entity the entity this collider is attached to
	 * @param shape the collision shape to use
	 * @throws NullPointerException if entity or shape is null
	 */
	public ColliderComponent(Entity entity, ICollisionShape shape) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
		this.shape = Objects.requireNonNull(shape, "Shape cannot be null");
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public ICollisionShape getCollisionShape() {
		return shape;
	}

	@Override
	public String toString() {
		return "ColliderComponent{" +
			"entity=" + entity.getID() +
			", shape=" + shape +
			'}';
	}
}