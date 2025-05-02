package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.ServiceLoader;

/**
 * Manager for the game level with true offscreen transition triggering.
 */
public class LevelManager implements ILevelSPI, IUpdate {
	private static final Logging LOGGER = Logging.createLogger("LevelManager", LoggingLevel.DEBUG);

	// Player dimensions (keeping hardcoded as requested)
	private static final float PLAYER_WIDTH = 16f;
	private static final float PLAYER_HEIGHT = 21f;
	private static final float PLAYER_SCALE = 1.1f;

	// Debug control
	private static final boolean DEBUG_ENABLED = true;
	private static final int DEBUG_LOG_FREQUENCY = 100;
	private int debugUpdateCounter = 0;

	// Transition state tracking
	private boolean northTransitionReady = false;
	private boolean eastTransitionReady = false;
	private boolean southTransitionReady = false;
	private boolean westTransitionReady = false;

	// Room and level management
	private static IRoomSPI roomSPI;
	private static HashMap<Integer, Room> roomMap = new HashMap<>();
	private static int currentRoom = -1;
	private static Level level;

	private final RoomTransitionSystem transitionSystem;

	public LevelManager() {
		LOGGER.debug("LevelManager constructor called");
		roomSPI = ServiceLoader.load(IRoomSPI.class).findFirst().orElse(null);

		if (roomSPI == null) {
			LOGGER.error("Failed to load IRoomSPI service!");
		} else {
			LOGGER.debug("Successfully loaded IRoomSPI service");
		}

		// Create the RoomTransitionSystem instance
		transitionSystem = RoomTransitionSystem.getInstance();
		LOGGER.debug("Created RoomTransitionSystem instance for LevelManager");
	}

	@Override
	public void generateLevel(int minRooms, int maxRooms, int width, int height) {
		debugLog("Generating level with minRooms=%d maxRooms=%d width=%d height=%d", minRooms, maxRooms, width, height);

		level = new Level(minRooms, maxRooms, width, height);
		level.createLayout();
		boolean[][] layout = level.getLayout();

		debugLog("Level layout created with %d rooms", countRooms(layout));

		int bossRoom = level.getEndRooms().isEmpty() ? -1 :
			level.getEndRooms().get((int) (Math.random() * level.getEndRooms().size()));

		debugLog("Boss room selected: %d", bossRoom);
		debugLog("Start room: %d", level.getStartRoom());

		// Create all rooms
		createRooms(layout, bossRoom);

		currentRoom = level.getStartRoom();
		LOGGER.debug("Level generation complete. Current room = " + currentRoom);

		// Reset transition flags
		resetTransitionTriggers();
	}

	/**
	 * Creates all rooms based on the level layout
	 */
	private void createRooms(boolean[][] layout, int bossRoom) {
		for (int x = 0; x < layout.length; x++) {
			if (layout[x][0]) { // This is a room
				RoomType type = RoomType.NORMAL;
				if (x == level.getStartRoom()) {
					type = RoomType.START;
				} else if (x == bossRoom) {
					type = RoomType.BOSS;
				}

				debugLog("Creating room at index " + x + " of type " + type +
					" with doors: N=" + layout[x][1] + ", E=" + layout[x][2] +
					", S=" + layout[x][3] + ", W=" + layout[x][4]);

				Room room = roomSPI.createRoom(type, layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				roomMap.put(x, room);

				// Log room entrances if debugging
				if (DEBUG_ENABLED) {
					logRoomEntrances(room);
				}

				if (x == level.getStartRoom()) {
					LOGGER.debug("Setting active scene to start room");
					SceneManager.getInstance().setActiveScene(room.getScene());
				}
			}
		}
	}

	/**
	 * Logs all entrances of a room for debugging
	 */
	private void logRoomEntrances(Room room) {
		Vector2D[] entrances = room.getEntrances();
		StringBuilder entrancesStr = new StringBuilder("Room entrances: ");
		for (int i = 0; i < entrances.length; i++) {
			entrancesStr.append(i).append("=");
			entrancesStr.append(entrances[i] == null ? "null" : entrances[i].toString());
			entrancesStr.append(", ");
		}
		LOGGER.debug(entrancesStr.toString());
	}

	private int countRooms(boolean[][] layout) {
		int count = 0;
		for (boolean[] room : layout) {
			if (room[0]) count++;
		}
		return count;
	}

	@Override
	public void update() {
		// Periodic debug info
		debugUpdateCounter++;
		debugLog("LevelManager.update() called " + debugUpdateCounter + " times");

		// Skip update if a transition is already in progress
		if (transitionSystem.isTransitioning()) {
			return;
		}

		// Find player entity in current scene
		Entity player = findPlayerEntity();
		if (player == null) {
			debugLog("No player entity found in the active scene");
			return;
		}

		TransformComponent transform = player.getComponent(TransformComponent.class);
		if (transform == null) {
			LOGGER.debug("Player entity has no TransformComponent");
			return;
		}

		float roomWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float roomHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

		// Debug player dimensions periodically
		debugLog("Player dimensions: " + PLAYER_WIDTH + "x" + PLAYER_HEIGHT);

		// Calculate transition offsets - ensure player is completely off screen
		float horizontalOffset = PLAYER_WIDTH * PLAYER_SCALE;
		float verticalOffset = PLAYER_HEIGHT * PLAYER_SCALE;

		// Check player position relative to room boundaries
		checkForOffscreenTransition(player, transform, roomWidth, roomHeight, horizontalOffset, verticalOffset);
	}

	/**
	 * Find player entity in active scene
	 */
	private Entity findPlayerEntity() {
		return Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class)
			.stream().findFirst().orElse(null);
	}

	/**
	 * Logs debug messages at controlled intervals
	 */
	private void debugLog(String message, Object... args) {
		if (DEBUG_ENABLED && debugUpdateCounter % DEBUG_LOG_FREQUENCY == 0) {
			LOGGER.debug(message, args);
		}
	}

	/**
	 * Reset all transition trigger states
	 */
	private void resetTransitionTriggers() {
		northTransitionReady = false;
		eastTransitionReady = false;
		southTransitionReady = false;
		westTransitionReady = false;
	}

	/**
	 * Check if player has moved completely off screen in any direction
	 */
	private void checkForOffscreenTransition(Entity player, TransformComponent transform,
											 float roomWidth, float roomHeight,
											 float horizontalOffset, float verticalOffset) {
		Vector2D position = transform.getPosition();
		Vector2D velocity = player.getComponent(PhysicsComponent.class).getVelocity();

		// Calculate player bounds based on center position
		float playerHalfWidth = PLAYER_WIDTH / 2;
		float playerHalfHeight = PLAYER_HEIGHT / 2;

		// Calculate player edges
		float playerLeft = position.x() - playerHalfWidth;
		float playerRight = position.x() + playerHalfWidth;
		float playerTop = position.y() - playerHalfHeight;
		float playerBottom = position.y() + playerHalfHeight;

		// EAST edge check
		if (!eastTransitionReady && playerLeft > roomWidth + horizontalOffset && velocity.x() > 0) {
			// Player is completely off the right edge
			LOGGER.debug("Player completely off EAST edge, triggering transition");
			eastTransitionReady = true;
			handleRoomTransition(player, RoomTransitionSystem.Direction.EAST);
		}
		// WEST edge check
		else if (!westTransitionReady && playerRight < -horizontalOffset && velocity.x() < 0) {
			// Player is completely off the left edge
			LOGGER.debug("Player completely off WEST edge, triggering transition");
			westTransitionReady = true;
			handleRoomTransition(player, RoomTransitionSystem.Direction.WEST);
		}
		// SOUTH edge check
		else if (!southTransitionReady && playerTop > roomHeight + verticalOffset && velocity.y() > 0) {
			// Player is completely off the bottom edge
			LOGGER.debug("Player completely off SOUTH edge, triggering transition");
			southTransitionReady = true;
			handleRoomTransition(player, RoomTransitionSystem.Direction.SOUTH);
		}
		// NORTH edge check
		else if (!northTransitionReady && playerBottom < -verticalOffset && velocity.y() < 0) {
			// Player is completely off the top edge
			LOGGER.debug("Player completely off NORTH edge, triggering transition");
			northTransitionReady = true;
			handleRoomTransition(player, RoomTransitionSystem.Direction.NORTH);
		}

		// Reset flags if player moves back into the room
		eastTransitionReady &= playerRight <= roomWidth;
		westTransitionReady &= playerLeft >= 0;
		southTransitionReady &= playerBottom <= roomHeight;
		northTransitionReady &= playerTop >= 0;
	}

	/**
	 * Handle transition to a new room
	 */
	private void handleRoomTransition(Entity player, RoomTransitionSystem.Direction direction) {
		int targetRoom = calculateTargetRoomId(direction);

		// Check if target room exists
		if (!roomMap.containsKey(targetRoom)) {
			LOGGER.debug("No room exists in direction: " + direction);
			return;
		}

		// Check if current room has a door in this direction
		if (!currentRoomHasDoor(direction)) {
			LOGGER.debug("Current room has no door in direction: " + direction);
			return;
		}

		// Start transition to new room
		LOGGER.debug("Starting " + direction + " transition to room " + targetRoom);
		transitionSystem.startTransition(
			currentRoom,
			targetRoom,
			roomMap.get(currentRoom),
			roomMap.get(targetRoom),
			direction,
			player
		);
		currentRoom = targetRoom;

		// Reset transition triggers for the new room
		resetTransitionTriggers();
	}

	/**
	 * Calculate the target room ID based on direction
	 */
	private int calculateTargetRoomId(RoomTransitionSystem.Direction direction) {
		return switch (direction) {
			case EAST -> currentRoom + 1;
			case WEST -> currentRoom - 1;
			case SOUTH -> currentRoom + level.getWidth();
			case NORTH -> currentRoom - level.getWidth();
			default -> currentRoom;
		};
	}

	/**
	 * Check if current room has a door in the specified direction
	 */
	private boolean currentRoomHasDoor(RoomTransitionSystem.Direction direction) {
		if (level == null || currentRoom < 0 || currentRoom >= level.getLayout().length) {
			return false;
		}

		boolean[] roomData = level.getLayout()[currentRoom];

		return switch (direction) {
			case NORTH -> roomData[1];
			case EAST -> roomData[2];
			case SOUTH -> roomData[3];
			case WEST -> roomData[4];
			default -> false;
		};
	}
}