// In CommonCollision
package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.Entity;

/**
 * Factory interface for creating collision components.
 */
public interface IColliderFactory {
	boolean addCircleCollider(Entity entity, float offsetX, float offsetY, float radius);
	boolean addTilemapCollider(Entity entity, int[][] collisionFlags);
}