package dk.sdu.sem.commonpathfinding;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.List;

/**
 * Service Provider Interface for pathfinding functionality.
 */
public interface IPathfindingSPI {
	/**
	 * Determines if there is line of sight between origin and target entity.
	 *
	 * @param origin Origin position
	 * @param direction Direction vector to check
	 * @param targetEntity The target entity to check visibility for
	 * @param obstacles List of physics layers that block visibility
	 * @return True if there is a direct line of sight to the target entity
	 */
	boolean hasLineOfSight(Vector2D origin, Vector2D direction, Entity targetEntity, List<PhysicsLayer> obstacles);

	/**
	 * Converts a grid position to world position.
	 *
	 * @param position Grid position
	 * @return World position
	 */
	Vector2D toWorldPosition(Vector2D position);
}