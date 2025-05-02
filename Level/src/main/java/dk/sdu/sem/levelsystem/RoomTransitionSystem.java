package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.rendering.FXRenderSystem;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

/**
 * System that handles transitions between rooms.
 * Uses pre-rendered room images for smoother transitions.
 */
public class RoomTransitionSystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("RoomTransitionSystem", LoggingLevel.DEBUG);

	private enum TransitionPhase {
		NONE,           // No transition active
		ROOM_SLIDING,   // Rooms are sliding (main transition)
		PLAYER_ENTRANCE // Player is entering the new room
	}

	public enum Direction {
		NORTH(0, new Vector2D(0, -1)),
		EAST(1, new Vector2D(1, 0)),
		SOUTH(2, new Vector2D(0, 1)),
		WEST(3, new Vector2D(-1, 0)),
		NONE(-1, Vector2D.ZERO);

		private final int value;
		private final Vector2D unitVector;

		Direction(int value, Vector2D unitVector) {
			this.value = value;
			this.unitVector = unitVector;
		}

		public int getValue() {
			return value;
		}

		public static String getDirectionName(int index) {
			return switch(index) {
				case 0 -> "north";
				case 1 -> "east";
				case 2 -> "south";
				case 3 -> "west";
				default -> "none";
			};
		}

		public Direction getOpposite() {
			if (this == NONE) return NONE;
			return values()[(ordinal() + 2) % 4];
		}

		/**
		 * Gets the offset for room positioning based on direction
		 */
		public Vector2D getRoomOffset(float roomWidth, float roomHeight) {
			return new Vector2D(
				unitVector.x() * roomWidth,
				unitVector.y() * roomHeight
			);
		}

		/**
		 * Gets the inverted offset for room positioning
		 */
		public Vector2D getInverseRoomOffset(float roomWidth, float roomHeight) {
			return new Vector2D(
				-unitVector.x() * roomWidth,
				-unitVector.y() * roomHeight
			);
		}

		/**
		 * Gets the entrance offset for player positioning
		 */
		public Vector2D getEntranceOffset(float offset) {
			return unitVector.scale(-offset);
		}
	}

	// Transition parameters
	private static final float ROOM_TRANSITION_DURATION = 0.4f;
	private static final float PLAYER_ENTRANCE_DURATION = 0.4f;
	private static final float ROOM_WIDTH = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
	private static final float ROOM_HEIGHT = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

	private static RoomTransitionSystem instance = null;

	// Transition state (
	// using static fields for now but would like to move away from this without breaking the functionality of course
	private static TransitionPhase currentPhase = TransitionPhase.NONE;
	private static Direction direction = Direction.NONE;
	private static float transitionProgress = 0.0f;

	private static int currentRoomId;
	private static int targetRoomId;
	private static Room targetRoom;

	private static Scene currentScene;
	private static Scene targetScene;

	private static Vector2D playerStartPosition;
	private static Vector2D playerTargetPosition;
	private static boolean playerInputEnabled = true;
	private static Entity playerEntity;

	public RoomTransitionSystem() {
		LOGGER.debug("RoomTransitionSystem constructor called");
	}

	public static RoomTransitionSystem getInstance() {
		if (instance == null) {
			LOGGER.debug("Creating new RoomTransitionSystem instance");
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

		RoomTransitionSystem.currentPhase = TransitionPhase.ROOM_SLIDING;
		RoomTransitionSystem.direction = direction;
		RoomTransitionSystem.currentRoomId = currentRoomId;
		RoomTransitionSystem.targetRoomId = targetRoomId;
		RoomTransitionSystem.targetRoom = targetRoom;
		RoomTransitionSystem.currentScene = currentRoom.getScene();
		RoomTransitionSystem.targetScene = targetRoom.getScene();
		RoomTransitionSystem.playerEntity = player;
		RoomTransitionSystem.transitionProgress = 0.0f;

		// Disable player input during transition
		disablePlayerInput();

		if (currentScene == null || targetScene == null) {
			LOGGER.error("Cannot start transition - scenes are null");
			handleTransitionError(new IllegalStateException("Null scenes"), "startTransition");
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
			Vector2D toPos = direction.getRoomOffset(ROOM_WIDTH, ROOM_HEIGHT);
			renderSystem.setRoomPositions(fromPos, toPos);

			LOGGER.debug("Started room transition from " + currentRoomId + " to " + targetRoomId + " in direction " + direction);
		} catch (Exception e) {
			handleTransitionError(e, "startTransition");
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

		float duration = currentPhase == TransitionPhase.ROOM_SLIDING
			? ROOM_TRANSITION_DURATION
			: PLAYER_ENTRANCE_DURATION;

		// Update transition progress and cap progress at 1.0
		transitionProgress += deltaTime / duration;
		transitionProgress = Math.min(transitionProgress, 1.0f);

		// Log progress every 10% increase (optional)
		if (transitionProgress % 0.1 < deltaTime / duration) {
			LOGGER.debug(currentPhase + " progress: " + Math.round(transitionProgress * 100) + "%");
		}

		// Execute the appropriate update based on current phase
		try {
			switch (currentPhase) {
				case ROOM_SLIDING -> updateRoomTransition(deltaTime);
				case PLAYER_ENTRANCE -> updatePlayerEntrance(deltaTime);
			}
		} catch (Exception e) {
			handleTransitionError(e, "update");
		}
	}

	/**
	 * Updates the room sliding transition phase
	 */
	private void updateRoomTransition(float deltaTime) {
		// Calculate current transition position (using smooth easing)
		float easedProgress = ease(transitionProgress);

		// Calculate offsets for both rooms
		Vector2D currentRoomOffset = Vector2D.ZERO.lerp(
			direction.getInverseRoomOffset(ROOM_WIDTH, ROOM_HEIGHT),
			easedProgress
		);

		Vector2D targetRoomOffset = direction.getRoomOffset(ROOM_WIDTH, ROOM_HEIGHT).lerp(
			Vector2D.ZERO,
			easedProgress
		);

		// Update room positions in the render system
		FXRenderSystem.getInstance().setRoomPositions(currentRoomOffset, targetRoomOffset);

		// If we've reached the end of this phase
		if (transitionProgress >= 1.0f) {
			LOGGER.debug("Room transition complete, starting player entrance animation");
			finishRoomTransition();
		}
	}

	/**
	 * Updates the player entrance animation phase
	 */
	private void updatePlayerEntrance(float deltaTime) {
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
			handleTransitionError(e, "finishRoomTransition");
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
			entrancePos = new Vector2D(ROOM_WIDTH / 2, ROOM_HEIGHT / 2); // Fallback
		}

		// Calculate a position just outside the room for the player to start from
		float offset = GameConstants.TILE_SIZE * 2; // 2 tiles outside the room
		Vector2D offsetFromEntrance = direction.getEntranceOffset(offset);
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

		Vector2D[] entrances = targetRoom.getEntrances();

		// Skip if direction is NONE
		if (direction == Direction.NONE) return null;

		// Use the opposite entrance (e.g., if going NORTH, use SOUTH entrance)
		Direction oppositeDirection = direction.getOpposite();
		Vector2D entrancePos = entrances[oppositeDirection.getValue()];

		LOGGER.debug("Using " + Direction.getDirectionName(oppositeDirection.getValue()) +
			" entrance of target room: " + entrancePos);

		return entrancePos;
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
		transitionProgress = 0.0f;

		enablePlayerInput();

		// Reset references to prevent memory leaks, pretty scuffed
		playerEntity = null;
		targetRoom = null;
		currentScene = null;
		targetScene = null;
		playerStartPosition = null;
		playerTargetPosition = null;

		// Re-enable player input
		enablePlayerInput();

		LOGGER.debug("Transition state reset, returned to normal gameplay");
	}

	/**
	 * Centralized error handling during transitions
	 */
	private static void handleTransitionError(Exception e, String phase) {
		LOGGER.error("Error during " + phase + ": " + e.getMessage());
		e.printStackTrace();

		// Specific recovery based on phase
		if (phase.equals("startTransition")) {
			// Special handling for transition start errors
			FXRenderSystem.getInstance().setTransitionMode(false, null, null);
		}

		// Always ensure player input is re-enabled
		enablePlayerInput();

		// Reset to known good state
		resetTransitionState();
	}

	/**
	 * Disable player input during transitions
	 */
	private static void disablePlayerInput() {
		if (playerInputEnabled) {
			playerInputEnabled = false;
			LOGGER.debug("Player input disabled during transition");

			// Find and disable the player controller
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

			// Re-enable the player controller
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
}