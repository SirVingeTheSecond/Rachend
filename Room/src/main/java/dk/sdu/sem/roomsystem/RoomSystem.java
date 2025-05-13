package dk.sdu.sem.roomsystem;

import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonlevel.room.IRoomClearedListener;
import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.animation.TileAnimation;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.components.TileAnimatorComponent;
import dk.sdu.sem.gamesystem.components.TilemapRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.*;
import java.util.stream.Collectors;

public class RoomSystem implements IRoomCreatedListener, IUpdate, IStart {
	private static HashMap<Scene, Room> roomHashMap = new HashMap<>();
	private static HashMap<Room, Set<TilemapColliderComponent>> colliderHashMap = new HashMap<>();

	@Override
	public void onRoomCreated(Room room) {
		spawnEnemies(room);
		createBarriers(room);
		roomHashMap.put(room.getScene(), room);
	}

	private boolean isSolid(Room room, int x, int y) {
		Set<TilemapColliderComponent> colliders = colliderHashMap.computeIfAbsent(room, k -> {
			Set<Entity> roomColliders = room.getScene().getEntitiesWithComponent(TilemapColliderComponent.class);
			return roomColliders.stream()
				.map(e -> e.getComponent(TilemapColliderComponent.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		});

		return colliders.stream().anyMatch(c -> c.isSolid(x, y));
	}

	private void createBarriers(Room room) {
		Entity barrier = new Entity();
		barrier.addComponent(new TransformComponent(Vector2D.ZERO, 0));

		Collection<Sprite> tileSprites = RoomAssetProvider.forceFieldMap.getAllSprites().values();
		List<IAssetReference<Sprite>> animatedSprites = new ArrayList<>();
		List<Float> frameDurations = new ArrayList<>();
		for (int i = 0; i < tileSprites.size(); i++) {
			animatedSprites.add(
				AssetFacade.createSpriteMapTileReference("force-field", i)
			);
			frameDurations.add(0.1f);
		}

		TileAnimation tileAnimation = new TileAnimation(animatedSprites, frameDurations, true);
		TileAnimatorComponent animator = new TileAnimatorComponent();
		animator.addTileAnimation(0, tileAnimation);
		barrier.addComponent(animator);

		int[][] collisionMap = new int[(int)GameConstants.WORLD_SIZE.x()][(int)GameConstants.WORLD_SIZE.y()];
		int[][] renderMap = new int[(int)GameConstants.WORLD_SIZE.x()][(int)GameConstants.WORLD_SIZE.y()];

		for (int[] r : renderMap) {
			Arrays.fill(r, -1);
		}

		for (int x = 0; x < GameConstants.WORLD_SIZE.x(); x++) {
			int y = (int) (GameConstants.WORLD_SIZE.y() - 1);
			if (!isSolid(room, x, 0))
			{
				collisionMap[x][0] = 1;
				renderMap[x][0] = 0;
			}

			if (!isSolid(room, x, y)) {
				collisionMap[x][y] = 1;
				renderMap[x][y] = 0;
			}
		}

		for (int y = 0; y < GameConstants.WORLD_SIZE.y(); y++) {
			int x = (int) (GameConstants.WORLD_SIZE.x() - 1);
			if (!isSolid(room, 0, y)) {
				collisionMap[0][y] = 1;
				renderMap[0][y] = 0;
			}

			if (!isSolid(room, x, y)) {
				collisionMap[x][y] = 1;
				renderMap[x][y] = 0;
			}
		}

		TilemapComponent tilemap = new TilemapComponent(
			"force-field",
			renderMap,
			GameConstants.TILE_SIZE
		);
		TilemapRendererComponent renderer = new TilemapRendererComponent(tilemap);
		renderer.setRenderLayer(GameConstants.LAYER_UI);
		barrier.addComponent(renderer);


		TilemapColliderComponent colliderComponent = new TilemapColliderComponent(
			barrier,
			collisionMap,
			GameConstants.TILE_SIZE
		);
		barrier.addComponent(new CollisionStateComponent());

		barrier.addComponent(tilemap);
		barrier.addComponent(colliderComponent);

		room.getScene().addEntity(barrier);

		room.setDoors(List.of(barrier));
	}

	private void spawnEnemies(Room room) {
		IEnemyFactory enemyFactory = ServiceLoader.load(IEnemyFactory.class).findFirst().orElse(null);

		List<Room.Zone> enemySpawns = room.getZones("ENEMY");

		if (enemyFactory != null && !enemySpawns.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				Vector2D point = enemySpawns.get((int) (Math.random() * enemySpawns.size())).getPosition();

				Entity enemy = enemyFactory.create(point, 100, 5, 3);
				room.getScene().addEntity(enemy);
			}
		}
	}

	@Override
	public void update() {
		int enemyCount = NodeManager.active().getNodes(EnemyNode.class).size();
		if (enemyCount > 0)
			return;

		Room room = roomHashMap.get(Scene.getActiveScene());
		if (room == null)
			return;

		if (room.getDoors().isEmpty())
			return;

		ServiceLoader.load(IRoomClearedListener.class).forEach(l -> l.onRoomCleared(room));

		for (Entity door : room.getDoors()) {
			Scene.getActiveScene().removeEntity(door);
		}

		room.setDoors(List.of());
	}

	@Override
	public void start() {
		//Clear hashmap on start (Restart)
		roomHashMap.clear();
		colliderHashMap.clear();
	}
}
