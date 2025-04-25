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
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
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

	private static final float PLAYER_WIDTH = 16f; // Should not be hardcoded
	private static final float PLAYER_HEIGHT = 21f; // Should not be hardcoded
	private static final float PLAYER_SCALE = 1.1f;

	// Track if transitions are ready to trigger
	private boolean northTransitionReady = false;
	private boolean eastTransitionReady = false;
	private boolean southTransitionReady = false;
	private boolean westTransitionReady = false;

	private static IRoomSPI roomSPI;
	private static HashMap<Integer, Room> roomMap = new HashMap<>();
	private static int currentRoom = -1;
	private static Level level;
	private RoomTransitionSystem transitionSystem;

	// Debugging flags
	private boolean debugInitialized = false;
	private int debugUpdateCounter = 0;

	public LevelManager() {
		LOGGER.debug("LevelManager constructor called");
		roomSPI = ServiceLoader.load(IRoomSPI.class).findFirst().orElse(null);

		if (roomSPI == null) {
			LOGGER.error("Failed to load IRoomSPI service!");
		} else {
			LOGGER.debug("Successfully loaded IRoomSPI service");
		}

		transitionSystem = RoomTransitionSystem.getInstance();
		if (transitionSystem == null) {
			LOGGER.error("Failed to get RoomTransitionSystem instance!");
		} else {
			LOGGER.debug("Successfully got RoomTransitionSystem instance");
		}
	}

	@Override
	public void generateLevel(int minRooms, int maxRooms, int width, int height) {
		LOGGER.debug("Generating level with minRooms=" + minRooms + ", maxRooms=" + maxRooms +
			", width=" + width + ", height=" + height);

		level = new Level(minRooms, maxRooms, width, height);
		level.createLayout();
		boolean[][] layout = level.getLayout();

		LOGGER.debug("Level layout created with " + countRooms(layout) + " rooms");

		int bossRoom = level.getEndRooms().isEmpty() ? -1 :
			level.getEndRooms().get((int) (Math.random() * level.getEndRooms().size()));

		LOGGER.debug("Boss room selected: " + bossRoom);
		LOGGER.debug("Start room: " + level.getStartRoom());

		for (int x = 0; x < layout.length; x++) {
			if (layout[x][0]) { // This is a room
				RoomType type = RoomType.NORMAL;
				if (x == level.getStartRoom()) {
					type = RoomType.START;
				} else if (x == bossRoom) {
					type = RoomType.BOSS;
				}

				LOGGER.debug("Creating room at index " + x + " of type " + type +
					" with doors: N=" + layout[x][1] + ", E=" + layout[x][2] +
					", S=" + layout[x][3] + ", W=" + layout[x][4]);

				Room room = roomSPI.createRoom(type, layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
				roomMap.put(x, room);

				// Debug room entrances
				Vector2D[] entrances = room.getEntrances();
				StringBuilder entrancesStr = new StringBuilder("Room entrances: ");
				for (int i = 0; i < entrances.length; i++) {
					entrancesStr.append(i).append("=");
					entrancesStr.append(entrances[i] == null ? "null" : entrances[i].toString());
					entrancesStr.append(", ");
				}
				LOGGER.debug(entrancesStr.toString());

				if (x == level.getStartRoom()) {
					LOGGER.debug("Setting active scene to start room");
					SceneManager.getInstance().setActiveScene(room.getScene());
				}
			}
		}

		currentRoom = level.getStartRoom();
		LOGGER.debug("Level generation complete. Current room = " + currentRoom);

		// Reset transition flags
		resetTransitionTriggers();
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
		// Initialize debug info once
		if (!debugInitialized) {
			LOGGER.debug("LevelManager.update() initialized");
			debugInitialized = true;
		}

		// Occasional debug info
		if (debugUpdateCounter++ % 100 == 0) {
			LOGGER.debug("LevelManager.update() called " + debugUpdateCounter + " times");
		}

		// Skip update if a transition is already in progress
		if (transitionSystem.isTransitioning()) {
			return;
		}

		Entity player = Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class)
			.stream().findFirst().orElse(null);

		if (player == null) {
			if (debugUpdateCounter % 100 == 0) {
				LOGGER.debug("No player entity found in the active scene");
			}
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
		if (debugUpdateCounter % 100 == 0) {
			LOGGER.debug("Player dimensions: " + PLAYER_WIDTH + "x" + PLAYER_HEIGHT);
		}

		// Calculate transition offsets - ensure player is completely off screen
		float horizontalOffset = PLAYER_WIDTH * PLAYER_SCALE;
		float verticalOffset = PLAYER_HEIGHT * PLAYER_SCALE;

		// Check player position relative to room boundaries
		checkForOffscreenTransition(player, transform, roomWidth, roomHeight, horizontalOffset, verticalOffset);
	}

	/**
	 * Reset transition trigger flags when entering a new room
	 */
	private void resetTransitionTriggers() {
		northTransitionReady = false;
		eastTransitionReady = false;
		southTransitionReady = false;
		westTransitionReady = false;
	}

	/**
	 * Checks if player is completely outside the room and initiates transition if appropriate
	 */
	private void checkForOffscreenTransition(Entity player, TransformComponent transform,
											 float roomWidth, float roomHeight,
											 float horizontalOffset, float verticalOffset) {
		Vector2D position = transform.getPosition();
		Vector2D velocity = player.getComponent(dk.sdu.sem.gamesystem.components.PhysicsComponent.class).getVelocity();

		// Calculate player bounds based on center position
		float playerHalfWidth = PLAYER_WIDTH / 2;
		float playerHalfHeight = PLAYER_HEIGHT / 2;

		// Calculate player edges
		float playerLeft = position.x() - playerHalfWidth;
		float playerRight = position.x() + playerHalfWidth;
		float playerTop = position.y() - playerHalfHeight;
		float playerBottom = position.y() + playerHalfHeight;

		// Check if player is completely off the screen in any direction
		// Only trigger transition if player has appropriate velocity (moving out of room)

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
		if (eastTransitionReady && playerRight <= roomWidth) {
			eastTransitionReady = false;
		}
		if (westTransitionReady && playerLeft >= 0) {
			westTransitionReady = false;
		}
		if (southTransitionReady && playerBottom <= roomHeight) {
			southTransitionReady = false;
		}
		if (northTransitionReady && playerTop >= 0) {
			northTransitionReady = false;
		}
	}

	/**
	 * Handles room transition in the specified direction
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
	 * Calculate target room ID based on direction
	 */
	private int calculateTargetRoomId(RoomTransitionSystem.Direction direction) {
		switch (direction) {
			case EAST: return currentRoom + 1;
			case WEST: return currentRoom - 1;
			case SOUTH: return currentRoom + level.getWidth();
			case NORTH: return currentRoom - level.getWidth();
			default: return currentRoom;
		}
	}

	/**
	 * Check if current room has a door in the specified direction
	 */
	private boolean currentRoomHasDoor(RoomTransitionSystem.Direction direction) {
		if (level == null || currentRoom < 0 || currentRoom >= level.getLayout().length) {
			return false;
		}

		boolean[] roomData = level.getLayout()[currentRoom];

		switch (direction) {
			case NORTH: return roomData[1];
			case EAST: return roomData[2];
			case SOUTH: return roomData[3];
			case WEST: return roomData[4];
			default: return false;
		}
	}
}