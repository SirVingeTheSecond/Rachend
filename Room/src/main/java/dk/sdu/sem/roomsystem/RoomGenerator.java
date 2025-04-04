package dk.sdu.sem.roomsystem;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.commonlevel.room.RoomLayer;
import dk.sdu.sem.commonlevel.room.RoomTileset;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.*;

public class RoomGenerator {
	int renderLayer = 0;
	int[][] collisionMap;

	public Scene createRoomScene(Room room) {
		Scene scene = new Scene(UUID.randomUUID().toString());
		try {
			renderLayer = 0;

			RoomData dto = room.getRoomData();
			collisionMap = new int[dto.width][dto.height];

			List<String> tileSets = createTileSets(dto);
			int[] cutPoints = getCutPoints(dto);

			for (RoomLayer layer : dto.layers) {
				switch (layer.name) {
					case "DOOR_NORTH":
						if (room.north())
							continue;
						break;
					case "DOOR_SOUTH":
						if (room.south())
							continue;
						break;
					case "DOOR_EAST":
						if (room.east())
							continue;
						break;
					case "DOOR_WEST":
						if (room.west())
							continue;
						break;
				}

				if (layer.name.equals("LAYER_FOREGROUND"))
					renderLayer = GameConstants.LAYER_FOREGROUND;

				for (int i = 0; i < dto.tilesets.size(); i++) {
					int finalI = i;
					List<Integer> psdLayer = layer.data.stream()
							.map(d -> (d > cutPoints[finalI] && (finalI == cutPoints.length - 1 || d < cutPoints[finalI + 1])) ? d - cutPoints[finalI] : 0)
							.toList();

					if (psdLayer.stream().anyMatch(d -> d != 0)) {

						RoomLayer layerDTO = new RoomLayer();
						layerDTO.data = psdLayer;
						layerDTO.name = layer.name;
						layerDTO.width = layer.width;
						layerDTO.height = layer.height;

						Entity tileMapEntity = createTileMapEntity(layerDTO, tileSets.get(i), dto.tilesets.get(i));
						scene.addEntity(tileMapEntity);
					}
				}

				renderLayer++;
			}

			//Add entity with combined collision tilemap
			Entity collisionEntity = new Entity();
			TilemapComponent tilemapComponent = new TilemapComponent(
				null,  // The exact name used in Assets.createSpriteSheet()
				collisionMap,  // Tile indices
				GameConstants.TILE_SIZE  // Tile size
			);
			TilemapColliderComponent collider = new TilemapColliderComponent(collisionMap);
			collider.setLayer(PhysicsLayer.OBSTACLE);
			collisionEntity.addComponent(collider);
			collisionEntity.addComponent(tilemapComponent);
			collisionEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

			scene.addEntity(collisionEntity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (!scene.getEntities().isEmpty())
			return scene;

		return null;
	}

	private int[] getCutPoints(RoomData dto) {
		int[] cutPoints = new int[dto.tilesets.size()];
		//Fill the list, first being 0
		for (int i = 0; i < dto.tilesets.size(); i++) {
			if (i == 0)
				cutPoints[i] = 0;
			else
				cutPoints[i] = cutPoints[i - 1] + dto.tilesets.get(i - 1).tileCount;

			System.out.println(cutPoints[i]);
		}
		return cutPoints;
	}

	private List<String> createTileSets(RoomData dto) {
		List<String> tileSets = new ArrayList<>();

		for (RoomTileset tileset : dto.tilesets) {
			String[] split = tileset.imagePath.split("/");
			String fileName = split[split.length - 1];
			String patternName = fileName.replace(".png", "");

			//AssetFacade.createSpriteSheet(patternName, image, 16, 16);
			AssetFacade.createSpriteMap(patternName)
					.withImagePath("Levels/tilesets/" + fileName)
					.withGrid(tileset.columns, tileset.rows(),tileset.tileWidth, tileset.tileHeight)
					.load();

			tileSets.add(patternName);
		}

		return tileSets;
	}

	public Entity createTileMapEntity(RoomLayer layerDTO, String tileMapName, RoomTileset tilesetDTO) {
		// Create the tilemap entity
		Entity tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Generate a map layout
		int[][] tileMap = getMapLayout(layerDTO);

		// Create tilemap component - using the exact name registered in GameAssetProvider
		TilemapComponent tilemapComponent = new TilemapComponent(
			tileMapName,  // The exact name used in Assets.createSpriteSheet()
			tileMap,  // Tile indices
			GameConstants.TILE_SIZE  // Tile size
		);

		tilemapComponent.setRenderLayer(renderLayer);

		tilemapEntity.addComponent(tilemapComponent);

		//Collisions
		updateCollisionMap(tilesetDTO, tileMap);
		return tilemapEntity;
	}

	public void updateCollisionMap(RoomTileset tilesetDTO, int[][] mapLayout) {

		int width = mapLayout.length;
		int height = mapLayout[0].length;

		List<Integer> collisionIDs = tilesetDTO.tiles.stream()
				.filter(t -> t.properties.stream()
						.anyMatch(p -> p.name.equals("collision") && (boolean)p.value))
				.map(t -> t.id).toList();


		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (collisionIDs.contains(mapLayout[i][j]))
					collisionMap[i][j] = 1;
			}
		}
	}

	public int[][] getMapLayout(RoomLayer layerDTO) {
		int height = layerDTO.height;
		int width = layerDTO.width;
		int[][] result = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				result[i][j] = layerDTO.data.get(j * width + i) - 1;

			}
		}
		return result;
	}
}
