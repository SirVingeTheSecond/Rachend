package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.data.TilemapNode;
import dk.sdu.sem.gamesystem.rendering.FXRenderSystem;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * System that handles transitions between rooms with optimized rendering.
 * Uses pre-rendered room images for smoother transitions.
 */
public class RoomTransitionSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("RoomTransitionSystem", LoggingLevel.DEBUG);

	// Transition phases
	private enum TransitionPhase {
		NONE,           // No transition active
		ROOM_SLIDING,   // Rooms are sliding (main transition)
		PLAYER_ENTRANCE // Player is entering the new room
	}

	// Static state for the transition to fix the ServiceLoader instantiation issue
	private static TransitionPhase currentPhase = TransitionPhase.NONE;
	private static Direction direction = Direction.NONE;
	private static float transitionProgress = 0.0f;
	private static final float roomTransitionDuration = 0.4f; // Duration for room transition (slightly faster)
	private static final float playerEntranceDuration = 0.4f; // Duration for player entrance animation

	// Room info - static to ensure data is shared between instances
	private static int currentRoomId;
	private static int targetRoomId;
	private static Room currentRoom;
	private static Room targetRoom;
	private static Scene currentScene;
	private static Scene targetScene;

	// Player entrance animation info
	private static Vector2D playerStartPosition;
	private static Vector2D playerTargetPosition;
	private static boolean playerInputEnabled = true;

	// Player entity reference
	private static Entity playerEntity;

	// Room dimensions
	private static final float roomWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
	private static final float roomHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

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

		if (currentPhase != TransitionPhase.NONE) {
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
		RoomTransitionSystem.currentPhase = TransitionPhase.ROOM_SLIDING;
		RoomTransitionSystem.direction = direction;
		RoomTransitionSystem.currentRoomId = currentRoomId;
		RoomTransitionSystem.targetRoomId = targetRoomId;
		RoomTransitionSystem.currentRoom = currentRoom;
		RoomTransitionSystem.targetRoom = targetRoom;
		RoomTransitionSystem.currentScene = currentRoom.getScene();
		RoomTransitionSystem.targetScene = targetRoom.getScene();
		RoomTransitionSystem.playerEntity = player;
		RoomTransitionSystem.transitionProgress = 0.0f;

		// Disable player input during transition
		disablePlayerInput();

		if (currentScene == null || targetScene == null) {
			LOGGER.error("Cannot start transition - scenes are null");
			currentPhase = TransitionPhase.NONE;
			enablePlayerInput(); // Re-enable input if transition fails
			return;
		}

		try {
			// Add both scenes to scene manager (don't set target as active yet)
			SceneManager.getInstance().addScene(targetScene);

			// Tell the render system that we're entering transition mode
			FXRenderSystem renderSystem = FXRenderSystem.getInstance();
			renderSystem.setTransitionMode(true, currentScene, targetScene);

			// Calculate initial positions for both rooms
			Vector2D fromPos = Vector2D.ZERO;
			Vector2D toPos = getDirectionOffset(direction);
			renderSystem.setRoomPositions(fromPos, toPos);

			LOGGER.debug("Started room transition from " + currentRoomId + " to " + targetRoomId + " in direction " + direction);
		} catch (Exception e) {
			LOGGER.error("Exception during startTransition: " + e.getMessage());
			e.printStackTrace();
			currentPhase = TransitionPhase.NONE;
			enablePlayerInput(); // Re-enable input if transition fails
			// Ensure render system is reset if an error occurs
			FXRenderSystem.getInstance().setTransitionMode(false, null, null);
		}
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
		if (currentPhase == TransitionPhase.NONE) {
			return;
		}

		float deltaTime = (float) Time.getDeltaTime();

		if (currentPhase == TransitionPhase.ROOM_SLIDING) {
			updateRoomTransition(deltaTime);
		} else if (currentPhase == TransitionPhase.PLAYER_ENTRANCE) {
			updatePlayerEntrance(deltaTime);
		}
	}

	/**
	 * Updates the room sliding transition phase
	 */
	private void updateRoomTransition(float deltaTime) {
		// Update transition progress
		transitionProgress += deltaTime / roomTransitionDuration;

		// Cap progress at 1.0
		if (transitionProgress > 1.0f) {
			transitionProgress = 1.0f;
		}

		// Log progress every 10% increase
		if (transitionProgress % 0.1 < deltaTime / roomTransitionDuration) {
			LOGGER.debug("Room transition progress: " + Math.round(transitionProgress * 100) + "%");
		}

		try {
			// Calculate current transition position (using smooth easing)
			float easedProgress = ease(transitionProgress);

			// Calculate offsets for both rooms
			Vector2D currentRoomOffset = Vector2D.ZERO.lerp(getInverseDirectionOffset(direction), easedProgress);
			Vector2D targetRoomOffset = getDirectionOffset(direction).lerp(Vector2D.ZERO, easedProgress);

			// Update room positions in the render system
			FXRenderSystem.getInstance().setRoomPositions(currentRoomOffset, targetRoomOffset);

			// If we've reached the end of this phase
			if (transitionProgress >= 1.0f) {
				LOGGER.debug("Room transition complete, starting player entrance animation");
				finishRoomTransition();
			}
		} catch (Exception e) {
			LOGGER.error("Exception during transition update: " + e.getMessage());
			e.printStackTrace();
			resetTransitionState();
		}
	}

	/**
	 * Updates the player entrance animation phase
	 */
	private void updatePlayerEntrance(float deltaTime) {
		// Update transition progress
		transitionProgress += deltaTime / playerEntranceDuration;

		// Cap progress at 1.0
		if (transitionProgress > 1.0f) {
			transitionProgress = 1.0f;
		}

		// Log progress every 10% increase
		if (transitionProgress % 0.1 < deltaTime / playerEntranceDuration) {
			// Using %% to properly escape % in the formatting string
			LOGGER.debug("Player entrance progress: " + Math.round(transitionProgress * 100) + "%%");
		}

		try {
			if (playerEntity != null) {
				TransformComponent transform = playerEntity.getComponent(TransformComponent.class);
				if (transform != null) {
					// Smoothly interpolate player position
					float easedProgress = easeOut(transitionProgress);
					Vector2D newPosition = playerStartPosition.lerp(playerTargetPosition, easedProgress);
					transform.setPosition(newPosition);
				}
			}

			// If we've reached the end of this phase
			if (transitionProgress >= 1.0f) {
				LOGGER.debug("Player entrance complete, resuming normal gameplay");
				finishPlayerEntrance();
			}
		} catch (Exception e) {
			LOGGER.error("Exception during player entrance update: " + e.getMessage());
			e.printStackTrace();
			finishPlayerEntrance(); // Force finish if there's an error
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
	 * Apply ease-out function for player entrance
	 */
	private static float easeOut(float t) {
		// Cubic ease-out: starts fast and slows down
		return 1 - (float)Math.pow(1 - t, 3);
	}

	/**
	 * Complete the room transition phase and begin player entrance animation
	 */
	private static void finishRoomTransition() {
		LOGGER.debug("Finishing room transition from room " + currentRoomId + " to " + targetRoomId);

		try {
			// Tell the render system that transition mode is over
			FXRenderSystem.getInstance().setTransitionMode(false, null, null);

			// Now make the target room active - this will transfer persisted entities
			LOGGER.debug("Setting active scene to target room");
			SceneManager.getInstance().setActiveScene(targetScene);

			if (playerEntity != null) {
				// Set up player entrance animation data
				setupPlayerEntranceAnimation();

				// Move to next phase
				currentPhase = TransitionPhase.PLAYER_ENTRANCE;
				transitionProgress = 0.0f;
			} else {
				resetTransitionState();
			}
		} catch (Exception e) {
			LOGGER.error("Exception during finishRoomTransition: " + e.getMessage());
			e.printStackTrace();
			resetTransitionState();
		}
	}

	/**
	 * Set up the player entrance animation after room transition
	 */
	private static void setupPlayerEntranceAnimation() {
		TransformComponent playerTransform = playerEntity.getComponent(TransformComponent.class);
		if (playerTransform == null) {
			LOGGER.error("Player has no TransformComponent for entrance animation");
			return;
		}

		// Determine entrance position in the new room based on transition direction
		Vector2D entrancePos = getEntrancePosition();
		if (entrancePos == null) {
			LOGGER.error("No valid entrance position found for direction " + direction);
			entrancePos = new Vector2D(roomWidth / 2, roomHeight / 2); // Fallback
		}

		// Calculate a position just outside the room for the player to start from
		Vector2D offsetFromEntrance = getOffsetFromEntrance(direction);
		playerStartPosition = entrancePos.add(offsetFromEntrance);
		playerTargetPosition = entrancePos;

		LOGGER.debug("Player entrance animation from " + playerStartPosition + " to " + playerTargetPosition);

		// Set player initial position
		playerTransform.setPosition(playerStartPosition);

		// Reset player velocity
		PhysicsComponent physics = playerEntity.getComponent(PhysicsComponent.class);
		if (physics != null) {
			physics.setVelocity(Vector2D.ZERO);
		}
	}

	/**
	 * Get the target entrance position in the new room
	 */
	private static Vector2D getEntrancePosition() {
		if (targetRoom == null) return null;

		Vector2D entrancePos = null;
		Vector2D[] entrances = targetRoom.getEntrances();

		// Get the entrance position based on transition direction
		switch (direction) {
			case NORTH:
				entrancePos = entrances[2]; // South entrance of target room
				LOGGER.debug("Using south entrance of target room: " + entrancePos);
				break;
			case SOUTH:
				entrancePos = entrances[0]; // North entrance of target room
				LOGGER.debug("Using north entrance of target room: " + entrancePos);
				break;
			case EAST:
				entrancePos = entrances[3]; // West entrance of target room
				LOGGER.debug("Using west entrance of target room: " + entrancePos);
				break;
			case WEST:
				entrancePos = entrances[1]; // East entrance of target room
				LOGGER.debug("Using east entrance of target room: " + entrancePos);
				break;
		}

		return entrancePos;
	}

	/**
	 * Calculate offset from entrance position to place player outside the room
	 */
	private static Vector2D getOffsetFromEntrance(Direction direction) {
		// Place player just outside the room based on direction
		float offset = GameConstants.TILE_SIZE * 2; // 2 tiles outside the room

		switch (direction) {
			case NORTH: return new Vector2D(0, offset);      // Start below the entrance
			case SOUTH: return new Vector2D(0, -offset);     // Start above the entrance
			case EAST: return new Vector2D(-offset, 0);      // Start to the left of the entrance
			case WEST: return new Vector2D(offset, 0);       // Start to the right of the entrance
			default: return Vector2D.ZERO;
		}
	}

	/**
	 * Complete the player entrance animation and return to normal gameplay
	 */
	private static void finishPlayerEntrance() {
		LOGGER.debug("Finishing player entrance animation");

		try {
			// Ensure player is exactly at the target position
			if (playerEntity != null) {
				TransformComponent transform = playerEntity.getComponent(TransformComponent.class);
				if (transform != null) {
					transform.setPosition(playerTargetPosition);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception during finishPlayerEntrance: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Always reset transition state and re-enable input
			resetTransitionState();
		}
	}

	/**
	 * Reset all transition state variables
	 */
	private static void resetTransitionState() {
		// Reset transition state
		currentPhase = TransitionPhase.NONE;
		direction = Direction.NONE;

		// Re-enable player input
		enablePlayerInput();

		LOGGER.debug("Transition state reset, returned to normal gameplay");
	}

	/**
	 * Disable player input during transitions
	 */
	private static void disablePlayerInput() {
		if (playerInputEnabled) {
			playerInputEnabled = false;
			LOGGER.debug("Player input disabled during transition");

			// Find and disable the player controller system
			if (playerEntity != null) {
				PlayerComponent playerComponent = playerEntity.getComponent(PlayerComponent.class);
				if (playerComponent != null) {
					playerComponent.setInputEnabled(false);
				}
			}
		}
	}

	/**
	 * Enable player input after transitions
	 */
	private static void enablePlayerInput() {
		if (!playerInputEnabled) {
			playerInputEnabled = true;
			LOGGER.debug("Player input re-enabled after transition");

			// Re-enable the player controller system
			if (playerEntity != null) {
				PlayerComponent playerComponent = playerEntity.getComponent(PlayerComponent.class);
				if (playerComponent != null) {
					playerComponent.setInputEnabled(true);
				}
			}
		}
	}

	/**
	 * Check if a transition is currently in progress
	 */
	public boolean isTransitioning() {
		return currentPhase != TransitionPhase.NONE;
	}

	/**
	 * Get the current transition direction
	 */
	public Direction getTransitionDirection() {
		return direction;
	}
}