package dk.sdu.sem.collisionsystem.utils;

import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.TilemapColliderNode;

/**
 * Utility class for validating different node types.
 */
public final class NodeValidator {

	private NodeValidator() {

	}

	/**
	 * Checks if a collider node is valid for collision detection.
	 * A valid node must have a properly initialized entity, transform, and collider.
	 */
	public static boolean isColliderNodeValid(ColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.collider != null &&
			node.collider.getShape() != null;
	}

	/**
	 * Checks if a tilemap node is valid for collision detection.
	 * A valid node must have a properly initialized entity, transform, tilemap and collider.
	 */
	public static boolean isTilemapNodeValid(TilemapColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.tilemap != null &&
			node.tilemapCollider != null &&
			node.tilemapCollider.getCollisionFlags() != null;
	}
}