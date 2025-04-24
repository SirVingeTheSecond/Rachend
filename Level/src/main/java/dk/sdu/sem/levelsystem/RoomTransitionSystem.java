package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.data.TilemapNode;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * System that handles transitions between rooms.
 */
public class RoomTransitionSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("RoomTransitionSystem", LoggingLevel.DEBUG);

	// Static state for the transition to fix the ServiceLoader instantiation issue
	private static boolean isTransitioning = false;
	private static Direction direction = Direction.NONE;
	private static float transitionProgress = 0.0f;
	private static final float transitionDuration = 0.5f; // Half a second for transition

	// Room info - static to ensure data is shared between instances
	private static int currentRoomId;
	private static int targetRoomId;
	private static Room currentRoom;
	private static Room targetRoom;
	private static Scene currentScene;
	private static Scene targetScene;

	// Player entity reference
	private static Entity playerEntity;

	// Room dimensions
	private static final float roomWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
	private static final float roomHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

	// Store original positions during transition
	private static final Map<Entity, Vector2D> originalPositions = new HashMap<>();

	// Store specific tilemap entities for special handling
	private static Map<Scene, Set<TilemapNode>> tilemapNodes = new HashMap<>();

	// Direction enum
	public enum Direction {
		NONE, NORTH, EAST, SOUTH, WEST
	}

	// Singleton instance
	private static RoomTransitionSystem instance = null;

	// Default constructor for ServiceLoader
	public RoomTransitionSystem() {
		LOGGER.debug("RoomTransitionSystem constructor called");
		// Always update the instance reference
		instance = this;
	}

	public static RoomTransitionSystem getInstance() {
		if (instance == null) {
			LOGGER.debug("Creating new RoomTransitionSystem instance on demand");
			instance = new RoomTransitionSystem();
		}
		return instance;
	}

	/**
	 * Start a room transition in the specified direction
	 */
	public void startTransition(int currentRoomId, int targetRoomId, Room currentRoom, Room targetRoom, Direction direction, Entity player) {
		LOGGER.debug("startTransition called with direction: " + direction);

		if (isTransitioning) {
			LOGGER.debug("Already transitioning, ignoring new request");
			return;
		}

		if (currentRoom == null || targetRoom == null) {
			LOGGER.error("Cannot start transition - rooms are null");
			return;
		}

		if (player == null) {
			LOGGER.error("Cannot start transition - player is null");
			return;
		}

		// Set static fields to ensure data is shared between instances
		RoomTransitionSystem.isTransitioning = true;
		RoomTransitionSystem.direction = direction;
		RoomTransitionSystem.currentRoomId = currentRoomId;
		RoomTransitionSystem.targetRoomId = targetRoomId;
		RoomTransitionSystem.currentRoom = currentRoom;
		RoomTransitionSystem.targetRoom = targetRoom;
		RoomTransitionSystem.currentScene = currentRoom.getScene();
		RoomTransitionSystem.targetScene = targetRoom.getScene();
		RoomTransitionSystem.playerEntity = player;
		RoomTransitionSystem.transitionProgress = 0.0f;

		if (currentScene == null || targetScene == null) {
			LOGGER.error("Cannot start transition - scenes are null");
			isTransitioning = false;
			return;
		}

		LOGGER.debug("Storing current state");

		// Clear any existing original positions
		originalPositions.clear();

		try {
			// Make both scenes visible (don't set target as active yet)
			SceneManager.getInstance().addScene(targetScene);

			// Find and cache all tilemap nodes for special handling
			cacheTilemapNodes();

			// Store original positions for regular entities
			storeOriginalPositions(currentScene);
			storeOriginalPositions(targetScene);

			Vector2D initialOffset = getDirectionOffset(direction);
			LOGGER.debug("Setting initial offset for target scene: " + initialOffset);

			// Set initial position for target room tiles
			offsetSceneEntities(targetScene, initialOffset);
			offsetTilemapNodes(tilemapNodes.get(targetScene), initialOffset);

			// Force invalidate tilemap renderer snapshots to ensure they get redrawn
			invalidateTilemapSnapshots();

			LOGGER.debug("Started room transition from " + currentRoomId + " to " + targetRoomId + " in direction " + direction);
		} catch (Exception e) {
			LOGGER.error("Exception during startTransition: " + e.getMessage());
			e.printStackTrace();
			isTransitioning = false;
		}
	}

	/**
	 * Cache all tilemap nodes for more efficient access
	 */
	private static void cacheTilemapNodes() {
		tilemapNodes.clear();

		if (currentScene != null) {
			Set<TilemapNode> currentTilemaps = NodeManager.active().getNodes(TilemapNode.class);
			tilemapNodes.put(currentScene, currentTilemaps);
			LOGGER.debug("Found " + currentTilemaps.size() + " tilemap nodes in current scene");
		}

		if (targetScene != null) {
			// Temporarily set target scene as active to get its nodes
			Scene previousActive = Scene.getActiveScene();
			Scene.setActiveScene(targetScene);

			Set<TilemapNode> targetTilemaps = NodeManager.active().getNodes(TilemapNode.class);
			tilemapNodes.put(targetScene, targetTilemaps);
			LOGGER.debug("Found " + targetTilemaps.size() + " tilemap nodes in target scene");

			// Restore previous active scene
			Scene.setActiveScene(previousActive);
		}
	}

	/**
	 * Invalidate all tilemap renderer snapshots to force redraw
	 */
	private static void invalidateTilemapSnapshots() {
		for (Set<TilemapNode> nodes : tilemapNodes.values()) {
			for (TilemapNode node : nodes) {
				if (node.renderer != null) {
					node.renderer.invalidateSnapshot();
					LOGGER.debug("Invalidated tilemap snapshot for entity: " + node.getEntity().getID());
				}
			}
		}
	}

	/**
	 * Store original positions of all entities in a scene
	 */
	private static void storeOriginalPositions(Scene scene) {
		if (scene == null) {
			LOGGER.error("Cannot store positions - scene is null");
			return;
		}

		int count = 0;
		for (Entity entity : scene.getEntities()) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			if (transform != null) {
				originalPositions.put(entity, transform.getPosition());
				count++;
			}
		}
		LOGGER.debug("Stored positions for " + count + " entities in scene " + scene.getName());
	}

	/**
	 * Get the initial offset for a scene based on transition direction
	 */
	private static Vector2D getDirectionOffset(Direction direction) {
		switch (direction) {
			case NORTH: return new Vector2D(0, -roomHeight);
			case SOUTH: return new Vector2D(0, roomHeight);
			case EAST: return new Vector2D(roomWidth, 0);
			case WEST: return new Vector2D(-roomWidth, 0);
			default: return Vector2D.ZERO;
		}
	}

	/**
	 * Updates the transition animation
	 */
	@Override
	public void update() {
		if (!isTransitioning) {
			return;
		}

		// Update transition progress
		float deltaTime = (float) Time.getDeltaTime();
		transitionProgress += deltaTime / transitionDuration;

		// Log progress every 10% increase
		if (transitionProgress % 0.1 < deltaTime / transitionDuration) {
			LOGGER.debug("Transition progress: " + Math.round(transitionProgress * 100) + "%");
		}

		if (transitionProgress >= 1.0f) {
			LOGGER.debug("Transition complete, finalizing");
			finishTransition();
			return;
		}

		try {
			// Calculate current transition position (using smooth easing)
			float easedProgress = ease(transitionProgress);

			// Calculate offsets for both scenes
			Vector2D currentSceneOffset = Vector2D.ZERO.lerp(getInverseDirectionOffset(direction), easedProgress);
			Vector2D targetSceneOffset = getDirectionOffset(direction).lerp(Vector2D.ZERO, easedProgress);

			// Apply offsets to all entities and tilemaps
			applyTransitionOffsets(currentScene, currentSceneOffset, targetScene, targetSceneOffset);

		} catch (Exception e) {
			LOGGER.error("Exception during transition update: " + e.getMessage());
			e.printStackTrace();
			isTransitioning = false;
		}
	}

	/**
	 * Apply transition offsets to all entities in both scenes
	 */
	private void applyTransitionOffsets(Scene currentScene, Vector2D currentOffset, Scene targetScene, Vector2D targetOffset) {
		// Apply to regular entities
		offsetSceneEntities(currentScene, currentOffset);
		offsetSceneEntities(targetScene, targetOffset);

		// Apply to tilemap entities
		Set<TilemapNode> currentTilemaps = tilemapNodes.get(currentScene);
		Set<TilemapNode> targetTilemaps = tilemapNodes.get(targetScene);

		if (currentTilemaps != null) {
			offsetTilemapNodes(currentTilemaps, currentOffset);
		}

		if (targetTilemaps != null) {
			offsetTilemapNodes(targetTilemaps, targetOffset);
		}

		// Invalidate snapshots to force redraw with new positions
		invalidateTilemapSnapshots();
	}

	/**
	 * Apply offset to all tilemap nodes
	 */
	private static void offsetTilemapNodes(Set<TilemapNode> nodes, Vector2D offset) {
		if (nodes == null) return;

		for (TilemapNode node : nodes) {
			Entity entity = node.getEntity();
			if (entity != null) {
				TransformComponent transform = entity.getComponent(TransformComponent.class);
				if (transform != null && originalPositions.containsKey(entity)) {
					Vector2D originalPos = originalPositions.get(entity);
					transform.setPosition(originalPos.add(offset));
				}
			}
		}
	}

	/**
	 * Get the offset for the current scene as it moves out
	 */
	private static Vector2D getInverseDirectionOffset(Direction direction) {
		switch (direction) {
			case NORTH: return new Vector2D(0, roomHeight);
			case SOUTH: return new Vector2D(0, -roomHeight);
			case EAST: return new Vector2D(-roomWidth, 0);
			case WEST: return new Vector2D(roomWidth, 0);
			default: return Vector2D.ZERO;
		}
	}

	/**
	 * Apply easing function to make transition smoother
	 */
	private static float ease(float t) {
		// Cubic easing: smooths the start and end of the transition
		return t < 0.5f ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
	}

	/**
	 * Offset all entities in a scene
	 */
	private static void offsetSceneEntities(Scene scene, Vector2D offset) {
		if (scene == null) {
			LOGGER.error("Cannot offset entities - scene is null");
			return;
		}

		for (Entity entity : scene.getEntities()) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			if (transform != null && originalPositions.containsKey(entity)) {
				Vector2D originalPos = originalPositions.get(entity);
				transform.setPosition(originalPos.add(offset));
			}
		}
	}

	/**
	 * Complete the transition and restore normal gameplay
	 */
	private static void finishTransition() {
		LOGGER.debug("Finishing transition from room " + currentRoomId + " to " + targetRoomId);

		try {
			// Restore original positions in both scenes
			restoreOriginalPositions();

			// Position player at correct entrance in target room
			if (playerEntity != null) {
				TransformComponent playerTransform = playerEntity.getComponent(TransformComponent.class);
				if (playerTransform != null) {
					Vector2D entrancePos = null;

					// Get the entrance position based on transition direction
					switch (direction) {
						case NORTH:
							entrancePos = targetRoom.getEntrances()[2]; // South entrance of target room
							LOGGER.debug("Using south entrance of target room: " + entrancePos);
							break;
						case SOUTH:
							entrancePos = targetRoom.getEntrances()[0]; // North entrance of target room
							LOGGER.debug("Using north entrance of target room: " + entrancePos);
							break;
						case EAST:
							entrancePos = targetRoom.getEntrances()[3]; // West entrance of target room
							LOGGER.debug("Using west entrance of target room: " + entrancePos);
							break;
						case WEST:
							entrancePos = targetRoom.getEntrances()[1]; // East entrance of target room
							LOGGER.debug("Using east entrance of target room: " + entrancePos);
							break;
					}

					if (entrancePos != null) {
						LOGGER.debug("Positioning player at entrance: " + entrancePos);
						playerTransform.setPosition(entrancePos);
						// Update the stored original position for the player
						originalPositions.put(playerEntity, entrancePos);
					} else {
						LOGGER.error("No valid entrance position found for direction " + direction);
						// Fallback position in center of room
						entrancePos = new Vector2D(roomWidth / 2, roomHeight / 2);
						playerTransform.setPosition(entrancePos);
					}
				}
			}

			// Now make the target room active - this will transfer persisted entities
			LOGGER.debug("Setting active scene to target room");
			SceneManager.getInstance().setActiveScene(targetScene);

			// Reset direction
			direction = Direction.NONE;

			// Clear original positions
			originalPositions.clear();
			tilemapNodes.clear();

			LOGGER.debug("Completed room transition to " + targetRoomId);
		} catch (Exception e) {
			LOGGER.error("Exception during finishTransition: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Ensure we always reset transition state
			isTransitioning = false;
		}
	}

	/**
	 * Restore original positions for all entities
	 */
	private static void restoreOriginalPositions() {
		LOGGER.debug("Restoring original positions for " + originalPositions.size() + " entities");

		for (Map.Entry<Entity, Vector2D> entry : originalPositions.entrySet()) {
			Entity entity = entry.getKey();
			Vector2D originalPos = entry.getValue();

			TransformComponent transform = entity.getComponent(TransformComponent.class);
			if (transform != null) {
				transform.setPosition(originalPos);
			}
		}

		// Invalidate snapshots one final time
		invalidateTilemapSnapshots();
	}

	/**
	 * Check if a transition is currently in progress
	 */
	public boolean isTransitioning() {
		return isTransitioning;
	}

	/**
	 * Get the current transition direction
	 */
	public Direction getTransitionDirection() {
		return direction;
	}
}