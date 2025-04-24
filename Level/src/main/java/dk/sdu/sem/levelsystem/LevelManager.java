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
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.ServiceLoader;

/**
 * Manager for the game level with enhanced debugging.
 */
public class LevelManager implements ILevelSPI, IUpdate {
	private static final Logging LOGGER = Logging.createLogger("LevelManager", LoggingLevel.DEBUG);

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

		// Debug update call occasionally
		if (debugUpdateCounter++ % 100 == 0) {
			LOGGER.debug("LevelManager.update() called " + debugUpdateCounter + " times");
		}

		// Skip update if a transition is already in progress
		if (transitionSystem.isTransitioning()) {
			LOGGER.debug("Transition in progress, skipping update");
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

		// Debugging player position occasionally
		if (debugUpdateCounter % 100 == 0) {
			LOGGER.debug("Player position: " + transform.getPosition() +
				", Room dimensions: " + roomWidth + "x" + roomHeight +
				", Current room: " + currentRoom);
		}

		// Check player position relative to doors
		boolean isNearDoor = false;

		// Check if player is at room edge
		if (transform.getPosition().x() > roomWidth) {
			LOGGER.debug("Player at EAST edge, position: " + transform.getPosition());
			isNearDoor = true;
			int targetRoom = currentRoom + 1;
			LOGGER.debug("Target room: " + targetRoom + ", Exists: " + roomMap.containsKey(targetRoom));

			if (roomMap.containsKey(targetRoom)) {
				boolean hasDoor = level.getLayout()[currentRoom][2]; // Check east door
				LOGGER.debug("Current room has east door: " + hasDoor);

				if (hasDoor) {
					LOGGER.debug("Starting EAST transition to room " + targetRoom);
					transitionSystem.startTransition(
						currentRoom,
						targetRoom,
						roomMap.get(currentRoom),
						roomMap.get(targetRoom),
						RoomTransitionSystem.Direction.EAST,
						player
					);
					currentRoom = targetRoom;
				} else {
					LOGGER.debug("No east door in current room, pushing player back");
					transform.setPosition(new Vector2D(roomWidth, transform.getPosition().y()));
				}
			} else {
				LOGGER.debug("No room to the east, pushing player back");
				transform.setPosition(new Vector2D(roomWidth, transform.getPosition().y()));
			}
		} else if (transform.getPosition().x() < 0) {
			LOGGER.debug("Player at WEST edge, position: " + transform.getPosition());
			isNearDoor = true;
			int targetRoom = currentRoom - 1;
			LOGGER.debug("Target room: " + targetRoom + ", Exists: " + roomMap.containsKey(targetRoom));

			if (roomMap.containsKey(targetRoom)) {
				boolean hasDoor = level.getLayout()[currentRoom][4]; // Check west door
				LOGGER.debug("Current room has west door: " + hasDoor);

				if (hasDoor) {
					LOGGER.debug("Starting WEST transition to room " + targetRoom);
					transitionSystem.startTransition(
						currentRoom,
						targetRoom,
						roomMap.get(currentRoom),
						roomMap.get(targetRoom),
						RoomTransitionSystem.Direction.WEST,
						player
					);
					currentRoom = targetRoom;
				} else {
					LOGGER.debug("No west door in current room, pushing player back");
					transform.setPosition(new Vector2D(0, transform.getPosition().y()));
				}
			} else {
				LOGGER.debug("No room to the west, pushing player back");
				transform.setPosition(new Vector2D(0, transform.getPosition().y()));
			}
		} else if (transform.getPosition().y() > roomHeight) {
			LOGGER.debug("Player at SOUTH edge, position: " + transform.getPosition());
			isNearDoor = true;
			int targetRoom = currentRoom + level.getWidth();
			LOGGER.debug("Target room: " + targetRoom + ", Exists: " + roomMap.containsKey(targetRoom));

			if (roomMap.containsKey(targetRoom)) {
				boolean hasDoor = level.getLayout()[currentRoom][3]; // Check south door
				LOGGER.debug("Current room has south door: " + hasDoor);

				if (hasDoor) {
					LOGGER.debug("Starting SOUTH transition to room " + targetRoom);
					transitionSystem.startTransition(
						currentRoom,
						targetRoom,
						roomMap.get(currentRoom),
						roomMap.get(targetRoom),
						RoomTransitionSystem.Direction.SOUTH,
						player
					);
					currentRoom = targetRoom;
				} else {
					LOGGER.debug("No south door in current room, pushing player back");
					transform.setPosition(new Vector2D(transform.getPosition().x(), roomHeight));
				}
			} else {
				LOGGER.debug("No room to the south, pushing player back");
				transform.setPosition(new Vector2D(transform.getPosition().x(), roomHeight));
			}
		} else if (transform.getPosition().y() < 0) {
			LOGGER.debug("Player at NORTH edge, position: " + transform.getPosition());
			isNearDoor = true;
			int targetRoom = currentRoom - level.getWidth();
			LOGGER.debug("Target room: " + targetRoom + ", Exists: " + roomMap.containsKey(targetRoom));

			if (roomMap.containsKey(targetRoom)) {
				boolean hasDoor = level.getLayout()[currentRoom][1]; // Check north door
				LOGGER.debug("Current room has north door: " + hasDoor);

				if (hasDoor) {
					LOGGER.debug("Starting NORTH transition to room " + targetRoom);
					transitionSystem.startTransition(
						currentRoom,
						targetRoom,
						roomMap.get(currentRoom),
						roomMap.get(targetRoom),
						RoomTransitionSystem.Direction.NORTH,
						player
					);
					currentRoom = targetRoom;
				} else {
					LOGGER.debug("No north door in current room, pushing player back");
					transform.setPosition(new Vector2D(transform.getPosition().x(), 0));
				}
			} else {
				LOGGER.debug("No room to the north, pushing player back");
				transform.setPosition(new Vector2D(transform.getPosition().x(), 0));
			}
		}

		if (isNearDoor && debugUpdateCounter % 10 == 0) {
			// Extra debug information for testing
			LOGGER.debug("Door transition check complete. Current room layout: ");
			if (currentRoom >= 0 && currentRoom < level.getLayout().length) {
				boolean[] roomData = level.getLayout()[currentRoom];
				LOGGER.debug("Room[" + currentRoom + "]: exists=" + roomData[0] +
					", N=" + roomData[1] + ", E=" + roomData[2] +
					", S=" + roomData[3] + ", W=" + roomData[4]);
			}
		}
	}
}