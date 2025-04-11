package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

/**
 * Node for entities with collider components.
 */
public class ColliderNode extends Node {
	public TransformComponent transform;
	public ColliderComponent collider;
	public PhysicsComponent physics; // Optional
	public TilemapComponent tilemap; // Optional

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		collider = entity.getComponent(ColliderComponent.class);
		physics = entity.getComponent(PhysicsComponent.class); // May be null
		tilemap = entity.getComponent(TilemapComponent.class); // May be null
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, ColliderComponent.class);
	}

	/**
	 * Checks if this is a tilemap collider node.
	 *
	 * @return True if this node has a tilemap component and the collider uses a GridShape
	 */
	public boolean isTilemapCollider() {
		return tilemap != null && collider.getShape() instanceof GridShape;
	}

	/**
	 * Checks if this is a physics collider node.
	 *
	 * @return True if this node has a physics component
	 */
	public boolean hasPhysics() {
		return physics != null;
	}

	/**
	 * Gets the grid shape if this is a tilemap collider.
	 *
	 * @return The grid shape, or null if this is not a tilemap collider
	 */
	public GridShape getGridShape() {
		if (isTilemapCollider()) {
			return (GridShape) collider.getShape();
		}
		return null;
	}
}