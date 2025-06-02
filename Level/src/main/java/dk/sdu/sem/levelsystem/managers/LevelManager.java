package dk.sdu.sem.levelsystem.managers;

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
import dk.sdu.sem.levelsystem.Level;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Manager for the game level.
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

	// Track if we're processing an event to prevent duplicates
	private boolean processingRoomExit = false;

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
			: level.getEndRooms().get(level.getEndRooms().size() - 1);

		createRooms(layout, bossRoom);
		currentRoomId = level.getStartRoom();
		LOGGER.debug("Level generation complete. Starting in room " + currentRoomId);
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

			// Create the room entity
			Entity roomEntity = new Entity();
			roomEntity.addComponent(new RoomComponent(x, type));
			roomEntity.addComponent(new SceneRenderStateComponent());
			setupRoomEntrances(roomEntity, room);

			// Set the scene BEFORE adding the entity
			if (x == level.getStartRoom()) {
				sceneManager.setActiveScene(room.getScene());
			}

			// Attach to the room's scene
			roomEntity.setScene(room.getScene());
			room.getScene().addEntity(roomEntity);

			roomEntities.put(x, roomEntity);

			LOGGER.debug("Created room " + x + " of type " + type);
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

	@Override
	public void update() {
		// Periodic debug info
		debugUpdateCounter++;

		// Find player entity
		Entity player = findPlayerEntity();
		if (player == null) {
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

		// Calculate player bounds
		Vector2D position = transform.getPosition();
		Vector2D velocity = physics.getVelocity();

		float playerHalfWidth = PLAYER_WIDTH / 2;
		float playerHalfHeight = PLAYER_HEIGHT / 2;

		// Calculate player edges
		float playerLeft = position.x() - playerHalfWidth;
		float playerRight = position.x() + playerHalfWidth;
		float playerTop = position.y() - playerHalfHeight;
		float playerBottom = position.y() + playerHalfHeight;

		// Horizontal and vertical offsets for transition
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
		if (exitDirection != null && !processingRoomExit) {
			LOGGER.debug("Player completely off " + exitDirection + " edge, dispatching boundary exit event");
			EventDispatcher.getInstance().dispatch(
				new RoomExitEvent(player, exitDirection, position)
			);
		}
	}

	@Override
	public void onEvent(RoomExitEvent event) {
		// Prevent processing multiple events at once
		if (processingRoomExit) {
			return;
		}

		processingRoomExit = true;

		try {
			Entity player = event.getEntity();
			Direction direction = event.getDirection();

			LOGGER.debug("Received RoomExitEvent for direction: " + direction);

			// Validate current room ID
			if (currentRoomId < 0) {
				LOGGER.error("Invalid current room ID: " + currentRoomId);
				return;
			}

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

			LOGGER.debug("Starting " + direction + " transition from room " + currentRoomId + " to room " + targetRoomId);

			// Dispatch the transition start event
			EventDispatcher.getInstance().dispatch(
				new TransitionStartEvent(currentRoomEntity, targetRoomEntity, direction, player)
			);

			// Update current room ID
			currentRoomId = targetRoomId;
		} finally {
			processingRoomExit = false;
		}
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