package dk.sdu.sem.collisionsystem.utils;

import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

/**
 * Utility class for validating different node types.
 * Used to ensure nodes and their components are properly initialized before use.
 */
public final class NodeValidator {
	private static final Logging LOGGER = Logging.createLogger("NodeValidator", LoggingLevel.DEBUG);


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
		if (node == null) {
			LOGGER.debug("Node is null");
			return false;
		}
		if (node.getEntity() == null) {
			LOGGER.debug("Entity is null");
			return false;
		}
		if (node.getEntity().getScene() == null) {
			LOGGER.debug("Scene is null");
			return false;
		}
		if (node.transform == null) {
			LOGGER.debug("Transform is null");
			return false;
		}
		if (node.collider == null) {
			LOGGER.debug("Collider is null");
			return false;
		}
		if (node.collider.getShape() == null) {
			LOGGER.debug("Shape is null");
			return false;
		}
		if (!node.collider.isEnabled()) {
			LOGGER.debug("Collider is disabled");
			return false;
		}
		return true;
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
			node.collider != null &&
			node.collider.getShape() != null &&
			node.collider.getShape() instanceof GridShape;
	}
}