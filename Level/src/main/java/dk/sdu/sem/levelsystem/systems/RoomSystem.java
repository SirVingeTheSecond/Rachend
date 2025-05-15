package dk.sdu.sem.levelsystem.systems;

import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonlevel.room.IRoomClearedListener;
import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.levelsystem.EnemyNode;
import dk.sdu.sem.levelsystem.factories.IBarrierFactory;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * System that manages room lifecycle events, enemy spawning, and creation of barriers.
 */
public class RoomSystem implements IRoomCreatedListener, IUpdate, IStart {
	private static final Logging LOGGER = Logging.createLogger("RoomSystem", LoggingLevel.DEBUG);

	// Maps to track room and collider relationships
	private static final HashMap<Scene, Room> roomSceneMap = new HashMap<>();
	private static final HashMap<Room, Set<TilemapColliderComponent>> colliderMap = new HashMap<>();

	private final IBarrierFactory barrierFactory;

	/**
	 * Default constructor - loads dependencies through ServiceLoader or creates defaults.
	 */
	public RoomSystem() {
		this.barrierFactory = ServiceLoader.load(IBarrierFactory.class).findFirst().orElse(null);
	}

	/**
	 * Constructor with explicit dependencies - useful for testing.
	 *
	 * @param barrierFactory The factory to use for creating barriers
	 */
	public RoomSystem(IBarrierFactory barrierFactory) {
		this.barrierFactory = barrierFactory;
	}

	/**
	 * Handles room creation events by initializing room entities and barriers.
	 *
	 * @param room The newly created room
	 */
	@Override
	public void onRoomCreated(Room room) {
		LOGGER.debug("Room created event received for " + room.getRoomType());
		spawnEnemies(room);
		createBarriers(room);
		roomSceneMap.put(room.getScene(), room);
	}

	/**
	 * Creates barrier entities around the perimeter of a room.
	 * Implements force field visual effects and collision detection.
	 *
	 * @param room The room to create barriers for
	 */
	private void createBarriers(Room room) {
		LOGGER.debug("Creating barriers for room: " + room.getScene().getName());

		try {
			Entity barrier = barrierFactory.createBarrier(room, this::isSolid);

			if (barrier != null) {
				// Add to scene and register as a door
				room.getScene().addEntity(barrier);
				room.setDoors(List.of(barrier));
				LOGGER.debug("Barrier added to room successfully");
			} else {
				LOGGER.error("Failed to create barrier for room: " + room.getScene().getName());
			}
		} catch (Exception e) {
			LOGGER.error("Exception creating barriers: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Checks if a tile at the given coordinates is solid based on room colliders.
	 *
	 * @param room The room to check
	 * @param x The x-coordinate to check
	 * @param y The y-coordinate to check
	 * @return true if the tile is solid, false otherwise
	 */
	public boolean isSolid(Room room, int x, int y) {
		Set<TilemapColliderComponent> colliders = colliderMap.computeIfAbsent(room, k -> {
			Set<Entity> roomColliders = room.getScene().getEntitiesWithComponent(TilemapColliderComponent.class);
			return roomColliders.stream()
				.map(e -> e.getComponent(TilemapColliderComponent.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		});

		return colliders.stream().anyMatch(c -> c.isSolid(x, y));
	}

	/**
	 * Spawns enemies in the room based on predefined spawn zones.
	 *
	 * @param room The room to spawn enemies in
	 */
	private void spawnEnemies(Room room) {
		LOGGER.debug("Spawning enemies for room type: " + room.getRoomType());

		// Get enemy factory from service loader
		IEnemyFactory enemyFactory = ServiceLoader.load(IEnemyFactory.class).findFirst().orElse(null);
		List<Room.Zone> enemySpawns = room.getZones("ENEMY");

		if (enemyFactory == null) {
			LOGGER.debug("No enemy factory available, skipping enemy spawning");
			return;
		}

		if (enemySpawns.isEmpty()) {
			LOGGER.debug("No enemy spawn zones in this room");
			return;
		}

		// Create 4 random enemies at spawn points
		try {
			for (int i = 0; i < 4; i++) {
				// Pick a random spawn point
				int spawnIndex = (int) (Math.random() * enemySpawns.size());
				Vector2D spawnPosition = enemySpawns.get(spawnIndex).getPosition();

				// Create the enemy entity
				Entity enemy = enemyFactory.create(spawnPosition, 100, 5, 3);
				room.getScene().addEntity(enemy);
				LOGGER.debug("Enemy spawned at position: " + spawnPosition);
			}
		} catch (Exception e) {
			LOGGER.error("Error spawning enemies: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Updates room state and detects when a room has been cleared of enemies.
	 * Triggers room cleared events when appropriate.
	 */
	@Override
	public void update() {
		// Get active scene and check for enemies
		int enemyCount = NodeManager.active().getNodes(EnemyNode.class).size();

		// If enemies remain, room is not cleared yet
		if (enemyCount > 0) {
			return;
		}

		// Get current room from active scene
		Room room = roomSceneMap.get(Scene.getActiveScene());
		if (room == null) {
			return;
		}

		// If doors are already removed, room was already cleared
		if (room.getDoors().isEmpty()) {
			return;
		}

		// Room is cleared, notify listeners
		LOGGER.debug("Room cleared, removing barriers");
		ServiceLoader.load(IRoomClearedListener.class).forEach(l -> l.onRoomCleared(room));

		// Remove door entities
		for (Entity door : room.getDoors()) {
			Scene.getActiveScene().removeEntity(door);
		}

		// Update room state
		room.setDoors(List.of());
	}

	/**
	 * Initializes system state when the game starts.
	 * Cleans up cached data for restarts.
	 */
	@Override
	public void start() {
		LOGGER.debug("Initializing RoomSystem");
		// Clear all maps on start (restart)
		roomSceneMap.clear();
		colliderMap.clear();
	}
}