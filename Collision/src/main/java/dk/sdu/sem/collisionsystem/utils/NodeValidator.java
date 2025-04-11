package dk.sdu.sem.collisionsystem.utils;

import dk.sdu.sem.collisionsystem.ColliderNode;

/**
 * Utility class for validating different node types.
 * Used to ensure nodes and their components are properly initialized before use.
 */
public final class NodeValidator {

	private NodeValidator() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Checks if a collider node is valid for collision detection.
	 * A valid node must have a properly initialized entity, transform, and collider.
	 *
	 * @param node The collider node to validate
	 * @return True if the node is valid, false otherwise
	 */
	public static boolean isColliderNodeValid(ColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.collider != null &&
			node.collider.getShape() != null &&
			node.collider.isEnabled();
	}

	/**
	 * Checks if a tilemap node is valid for collision detection.
	 * A valid node must have a properly initialized entity, transform, tilemap and collider.
	 *
	 * @param node The tilemap node to validate
	 * @return True if the node is valid, false otherwise
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