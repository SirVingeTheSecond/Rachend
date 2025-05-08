package dk.sdu.sem.levelsystem;

import dk.sdu.sem.commonlevel.Direction;
import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonlevel.IRoomSPI;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.events.EventDispatcher;
import dk.sdu.sem.commonsystem.events.EventListener;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.commonlevel.components.RoomComponent;
import dk.sdu.sem.commonlevel.components.SceneRenderStateComponent;
import dk.sdu.sem.commonlevel.components.TransitionComponent;
import dk.sdu.sem.commonlevel.events.RoomExitEvent;
import dk.sdu.sem.commonlevel.events.TransitionStartEvent;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Manager for the game level with room boundaries and transition triggering.
 */
public class LevelManager implements ILevelSPI, IUpdate, EventListener<RoomExitEvent> {
	private static final Logging LOGGER = Logging.createLogger("LevelManager", LoggingLevel.DEBUG);

	// Debug
	private static final boolean DEBUG_ENABLED = true;
	private static final int DEBUG_LOG_FREQUENCY = 100;
	private int debugUpdateCounter = 0;

	// Room and level
	private static IRoomSPI roomSPI;
	private final Map<Integer, Entity> roomEntities = new HashMap<>();
	private final Map<Integer, Room> roomMap = new HashMap<>();
	private int currentRoomId = -1;
	private Level level;

	// Player
	private final float PLAYER_WIDTH = 16;
	private final float PLAYER_HEIGHT = 21;

	public LevelManager() {
		LOGGER.debug("LevelManager constructor called");
		roomSPI = ServiceLoader.load(IRoomSPI.class).findFirst().orElse(null);

		if (roomSPI == null) {
			LOGGER.error("Failed to load IRoomSPI service!");
		} else {
			LOGGER.debug("Successfully loaded IRoomSPI service");
		}

		// Register for exit events
		EventDispatcher.getInstance().addListener(RoomExitEvent.class, this);
	}

	@Override
	public void generateLevel(int minRooms, int maxRooms, int width, int height) {
		level = new Level(minRooms, maxRooms, width, height);
		level.createLayout();
		boolean[][] layout = level.getLayout();

		int bossRoom = level.getEndRooms().isEmpty()
			? -1
			: level.getEndRooms()
			.get((int)(Math.random() * level.getEndRooms().size()));

		createRooms(layout, bossRoom);
		currentRoomId = level.getStartRoom();
	}

	/**
	 * Creates all rooms based on the level layout
	 */
	private void createRooms(boolean[][] layout, int bossRoom) {
		SceneManager sceneManager = SceneManager.getInstance();

		for (int x = 0; x < layout.length; x++) {
			if (!layout[x][0]) continue;       // not a room cell

			RoomType type = (x == level.getStartRoom())
				? RoomType.START
				: (x == bossRoom ? RoomType.BOSS : RoomType.NORMAL);

			Room room = roomSPI.createRoom(
				type, layout[x][1], layout[x][2], layout[x][3], layout[x][4]);
			roomMap.put(x, room);

			/* ------------ critical order change ------------ */
			if (x == level.getStartRoom()) {
				// Activate scene *before* we attach any entities to it
				sceneManager.setActiveScene(room.getScene());
			}
			/* ------------------------------------------------ */

			// Create the synthetic room entity
			Entity roomEntity = new Entity();
			roomEntity.addComponent(new RoomComponent(x, type));
			roomEntity.addComponent(new SceneRenderStateComponent());
			setupRoomEntrances(roomEntity, room);

			// Attach to the room’s scene – NodeManager is now correct
			roomEntity.setScene(room.getScene());
			room.getScene().addEntity(roomEntity);

			roomEntities.put(x, roomEntity);
		}
	}

	/**
	 * Sets up entrance positions for a room entity
	 */
	private void setupRoomEntrances(Entity roomEntity, Room room) {
		RoomComponent roomComponent = roomEntity.getComponent(RoomComponent.class);
		Vector2D[] entrances = room.getEntrances();

		for (int i = 0; i < entrances.length; i++) {
			if (entrances[i] != null) {
				Direction direction = Direction.values()[i];
				roomComponent.setEntrance(direction, entrances[i]);
				roomComponent.setDoor(direction, true);
			}
		}
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

		// Skip if a transition is already in progress
		Entity player = findPlayerEntity();
		if (player == null) {
			debugLog("No player entity found in the active scene");
			return;
		}

		// Check if player is transitioning - if so, skip boundary checks
		if (player.hasComponent(TransitionComponent.class) &&
			player.getComponent(TransitionComponent.class).isTransitioning()) {
			return;
		}

		// Check for player crossing room boundaries
		checkPlayerBoundaries(player);
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
	 * Check if player has crossed room boundaries
	 */
	private void checkPlayerBoundaries(Entity player) {
		TransformComponent transform = player.getComponent(TransformComponent.class);
		if (transform == null) {
			return;
		}

		PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
		if (physics == null) {
			return;
		}

		float roomWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float roomHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

		// ==================================================

		// Calculate player bounds
		// Should not be re-calculated everytime
		Vector2D position = transform.getPosition();
		Vector2D velocity = physics.getVelocity();

		float playerHalfWidth = PLAYER_WIDTH / 2;
		float playerHalfHeight = PLAYER_HEIGHT / 2;

		// Calculate player edges
		float playerLeft = position.x() - playerHalfWidth;
		float playerRight = position.x() + playerHalfWidth;
		float playerTop = position.y() - playerHalfHeight;
		float playerBottom = position.y() + playerHalfHeight;

		// ==================================================

		// Horizontal offset for transition
		float horizontalOffset = PLAYER_WIDTH;
		float verticalOffset = PLAYER_HEIGHT;

		// Check if player has crossed boundaries
		Direction exitDirection = null;

		// EAST edge check
		if (playerLeft > roomWidth + horizontalOffset && velocity.x() > 0) {
			exitDirection = Direction.EAST;
		}
		// WEST edge check
		else if (playerRight < -horizontalOffset && velocity.x() < 0) {
			exitDirection = Direction.WEST;
		}
		// SOUTH edge check
		else if (playerTop > roomHeight + verticalOffset && velocity.y() > 0) {
			exitDirection = Direction.SOUTH;
		}
		// NORTH edge check
		else if (playerBottom < -verticalOffset && velocity.y() < 0) {
			exitDirection = Direction.NORTH;
		}

		// If player has exited, dispatch event
		if (exitDirection != null) {
			LOGGER.debug("Player completely off " + exitDirection + " edge, dispatching boundary exit event");
			EventDispatcher.getInstance().dispatch(
				new RoomExitEvent(player, exitDirection, position)
			);
		}
	}

	@Override
	public void onEvent(RoomExitEvent event) {
		Entity player = event.getEntity();
		Direction direction = event.getDirection();

		// Get the current room entity
		Entity currentRoomEntity = roomEntities.get(currentRoomId);
		if (currentRoomEntity == null) {
			LOGGER.error("No room entity found for current room ID: " + currentRoomId);
			return;
		}

		// Check if current room has a door in this direction
		RoomComponent roomComponent = currentRoomEntity.getComponent(RoomComponent.class);
		if (roomComponent == null || !roomComponent.hasDoor(direction)) {
			LOGGER.debug("Current room has no door in direction: " + direction);
			return;
		}

		// Calculate target room ID based on direction
		int targetRoomId = calculateTargetRoomId(direction);

		// Check if target room exists
		Entity targetRoomEntity = roomEntities.get(targetRoomId);
		if (targetRoomEntity == null) {
			LOGGER.debug("No room exists in direction: " + direction);
			return;
		}

		LOGGER.debug("Starting " + direction + " transition to room " + targetRoomId);

		// Set player as transitioning
		TransitionComponent transitionComp = player.getComponent(TransitionComponent.class);
		if (transitionComp == null) {
			transitionComp = new TransitionComponent();
			player.addComponent(transitionComp);
		}
		transitionComp.setTransitioning(true);

		EventDispatcher.getInstance().dispatch(
			new TransitionStartEvent(currentRoomEntity, targetRoomEntity, direction, player)
		);

		currentRoomId = targetRoomId;
	}

	/**
	 * Calculate the target room ID based on direction
	 */
	private int calculateTargetRoomId(Direction direction) {
		return switch (direction) {
			case EAST -> currentRoomId + 1;
			case WEST -> currentRoomId - 1;
			case SOUTH -> currentRoomId + level.getWidth();
			case NORTH -> currentRoomId - level.getWidth();
			default -> currentRoomId;
		};
	}
}