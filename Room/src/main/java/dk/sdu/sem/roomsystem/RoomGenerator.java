package dk.sdu.sem.roomsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.sem.commonlevel.room.*;
import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomData;
import dk.sdu.sem.commonlevel.room.RoomLayer;
import dk.sdu.sem.commonlevel.room.RoomTileset;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.enemy.IEnemyFactory;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.components.TilemapRendererComponent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RoomGenerator {
	private boolean DEBUG_ZONES = false;

	int renderLayer = 0;
	int[][] collisionMap;
	Room roomScene;

	public Room createRoomScene(RoomInfo room) {
		Scene scene = new Scene(UUID.randomUUID().toString());
		roomScene = new Room(scene);
		renderLayer = 0;

		RoomData dto = room.getRoomData();
		collisionMap = new int[dto.width][dto.height];

		List<String> tileSets = createTileSets(dto);
		int[] cutPoints = getCutPoints(dto);

		for (RoomLayer layer : dto.layers) {
			// Skip the DOOR layers if they are open in the room
			switch (layer.name) {
				case "DOOR_NORTH":
					if (room.north()) continue;
					break;
				case "DOOR_SOUTH":
					if (room.south()) continue;
					break;
				case "DOOR_EAST":
					if (room.east()) continue;
					break;
				case "DOOR_WEST":
					if (room.west()) continue;
					break;
			}

			if (layer.name.equals("LAYER_FOREGROUND"))
				renderLayer = GameConstants.LAYER_FOREGROUND;

			if (layer.name.equals("LAYER_FLOOR")) {
				generateProceduralFloor(layer);
				renderLayer++;
			}

			for (int i = 0; i < dto.tilesets.size(); i++) {
				int finalI = i;
				// Split each layer into multiple sub layers based on the tileset usage
				// Each tileset used becomes a separate layer
				List<Integer> psdLayer = layer.data.stream()
					.map(d -> (d > cutPoints[finalI] && (finalI == cutPoints.length - 1 || d < cutPoints[finalI + 1])) ? d - cutPoints[finalI] : 0)
					.toList();

				// Check if generated layer has any tiles d != 0
				if (psdLayer.stream().anyMatch(d -> d != 0)) {
					// Copy the original layer but change data
					RoomLayer layerDTO = new RoomLayer();
					layerDTO.data = psdLayer;
					layerDTO.name = layer.name;
					layerDTO.width = layer.width;
					layerDTO.height = layer.height;

					if (layer.name.equals("ZONES")) {
						processZones(layerDTO, scene);
						if (!DEBUG_ZONES)
							continue;
					}

					Entity tileMapEntity = createTileMapEntity(layerDTO, tileSets.get(i), dto.tilesets.get(i));
					scene.addEntity(tileMapEntity);
				}
			}

			renderLayer++;
		}

		ServiceLoader<IColliderFactory> colliderFactoryLoader = ServiceLoader.load(IColliderFactory.class);
		IColliderFactory colliderFactory = colliderFactoryLoader.findFirst().orElseThrow(() ->
			new IllegalStateException("No IColliderFactory implementation found")
		);
		Entity collisionEntity = colliderFactory.createTilemapColliderEntity(
			new Vector2D(0, 0), collisionMap, PhysicsLayer.OBSTACLE
		);
		scene.addEntity(collisionEntity);

		if (!scene.getEntities().isEmpty())
			return roomScene;

		return null;
	}

	int[][][] normalFloor = {
		{ //Floor type 1
			{0, 1, 17, 18, 34, 35, 3, 4, 20, 21, 37, 38}, //Base Floor 1
			{239, 240, 256, 257}, //Middle
			{221}, //Corner topLeft
			{222, 223}, //Top edge
			{224}, //Corner topRight
			{241, 258}, //Right edge
			{275}, //Corner bottomRight
			{273, 274}, //Bottom edge
			{272}, //Corner bottomLeft
			{238, 255}, //Left edge
		},
		{
			{68, 69, 85, 86, 119, 120, 122, 123, 136, 137, 139, 140},
			{239, 240, 256, 257}, //Middle
			{221}, //Corner topLeft
			{222, 223}, //Top edge
			{224}, //Corner topRight
			{241, 258}, //Right edge
			{275}, //Corner bottomRight
			{273, 274}, //Bottom edge
			{272}, //Corner bottomLeft
			{238, 255}, //Left edge
		}
		//Floor type 2

	};
	private void generateProceduralFloor(RoomLayer layer) {
		try {
			NoiseGenerator holeGenerator = new NoiseGenerator();
			NoiseGenerator floorGenerator = new NoiseGenerator();
			ObjectMapper mapper = new ObjectMapper();
			RoomTileset tileset = mapper.readValue(new File("Levels/tilesets/ProceduralFloor.tsj"), RoomTileset.class);

			AssetFacade.createSpriteMap("ProceduralFloor")
				.withImagePath("Levels/tilesets/" + "proceduralfloor.png")
				.withGrid(tileset.columns, tileset.rows(),tileset.tileWidth, tileset.tileHeight)
				.load();

			int height = layer.height;
			int width = layer.width;
			int[][] result = new int[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					double value = holeGenerator.perlin(i / 5f,j / 5f);
					if (value > 0.6)
						result[i][j] = 1; //Hole
					else
						result[i][j] = 0; //Normal floor
				}
			}

			int[][] tilesMap = new int[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					double value = floorGenerator.perlin(i/10f, j/10f);
					if (value > 0.5)
						getHoleTile(result, tilesMap,i,j,normalFloor[0]);
					else
						getHoleTile(result, tilesMap,i,j,normalFloor[1]);
				}
			}

			Entity tilemapEntity = new Entity();
			tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

			// Create tilemap data component
			TilemapComponent tilemapComponent = new TilemapComponent(
				"ProceduralFloor",  // The tileset ID used in Assets.createSpriteSheet()
				tilesMap,      // Tile indices
				GameConstants.TILE_SIZE  // Tile size
			);
			tilemapEntity.addComponent(tilemapComponent);

			// Create tilemap renderer component
			TilemapRendererComponent rendererComponent = new TilemapRendererComponent(tilemapComponent);
			rendererComponent.setRenderLayer(renderLayer);
			tilemapEntity.addComponent(rendererComponent);

			roomScene.getScene().addEntity(tilemapEntity);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void getHoleTile(int[][] layout, int[][] tileMap, int x, int y, int[][] tiles) {
		int width = layout.length;
		int height = layout[0].length;
		if (x < 0 || y < 0 || x >= width || y >= height)
			return;

		//Binary representation
		int neighbours = 0;

		int tileId = 0;
		if (layout[x][y] == 1) {
			//Hole

			if (y - 1 < 0 || layout[x][y - 1] == 1)
				neighbours += 1;

			if (x + 1 >= width || layout[x + 1][y] == 1)
				neighbours += 2;

			if (y + 1 >= height || layout[x][y + 1] == 1)
				neighbours += 4;

			if (x - 1 < 0 || layout[x - 1][y] == 1)
				neighbours += 8;

			switch (neighbours) {
				//Whole
				case 1:
					//Remove self, and recalculate
					layout[x][y] = 0;
					getHoleTile(layout, tileMap, x, y - 1, tiles);
					getHoleTile(layout, tileMap, x, y, tiles);
					return;
				case 2:
					//Remove self, and recalculate
					layout[x][y] = 0;
					getHoleTile(layout, tileMap, x + 1, y, tiles);
					getHoleTile(layout, tileMap, x, y, tiles);
					return;
				case 4:
					//Remove self, and recalculate
					layout[x][y] = 0;
					getHoleTile(layout, tileMap, x, y + 1, tiles);
					getHoleTile(layout, tileMap, x, y, tiles);
					return;
				case 8:
					//Remove self, and recalculate
					layout[x][y] = 0;
					getHoleTile(layout, tileMap, x - 1, y, tiles);
					getHoleTile(layout, tileMap, x, y, tiles);
					return;
				case 5:
					//Remove self, and recalculate
					layout[x][y] = 0;
					getHoleTile(layout, tileMap, x, y - 1, tiles);
					getHoleTile(layout, tileMap, x, y + 1, tiles);
					getHoleTile(layout, tileMap, x, y, tiles);
					return;
				case 10:
					//Remove self, and recalculate
					layout[x][y] = 0;
					getHoleTile(layout, tileMap, x - 1, y, tiles);
					getHoleTile(layout, tileMap, x + 1, y, tiles);
					getHoleTile(layout, tileMap, x, y, tiles);
					return;
				case 15: tileId = getRandomTile(tiles[1]); break;
				//Edges
				case 14: tileId = getRandomTile(tiles[3]); break; //top
				case 13: tileId = getRandomTile(tiles[5]); break; //right
				case 11: tileId = getRandomTile(tiles[7]); break; //bottom
				case 7: tileId = getRandomTile(tiles[9]); break; //left
				//Corners
				case 6: tileId = getRandomTile(tiles[2]); break; //topLeft
				case 12: tileId = getRandomTile(tiles[4]); break; //topRight
				case 9: tileId = getRandomTile(tiles[6]); break; //bottomRight
				case 3: tileId = getRandomTile(tiles[8]); break; //bottomLeft
			}
		} else {
			tileId = getRandomTile(tiles[0]);
		}

		tileMap[x][y] = tileId;
	}

	private int getRandomTile(int[] tiles) {
		return tiles[(int) (Math.random() * tiles.length)];
	}


	/// Gets a list of points for when tile indexes change tilemap
	private int[] getCutPoints(RoomData dto) {
		int[] cutPoints = new int[dto.tilesets.size()];
		// Fill the list, first being 0
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

			// AssetFacade.createSpriteSheet(patternName, image, 16, 16);
			AssetFacade.createSpriteMap(patternName)
					.withImagePath("Levels/tilesets/" + fileName)
					.withGrid(tileset.columns, tileset.rows(),tileset.tileWidth, tileset.tileHeight)
					.load();

			tileSets.add(patternName);
		}

		return tileSets;
	}

	private Entity createTileMapEntity(RoomLayer layerDTO, String tileMapName, RoomTileset tilesetDTO) {
		// Create the tilemap entity
		Entity tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Generate a map layout
		int[][] tileMap = getMapLayout(layerDTO);

		// Create tilemap data component
		TilemapComponent tilemapComponent = new TilemapComponent(
			tileMapName,  // The tileset ID used in Assets.createSpriteSheet()
			tileMap,      // Tile indices
			GameConstants.TILE_SIZE  // Tile size
		);
		tilemapEntity.addComponent(tilemapComponent);

		// Create tilemap renderer component
		TilemapRendererComponent rendererComponent = new TilemapRendererComponent(tilemapComponent);
		rendererComponent.setRenderLayer(renderLayer);
		tilemapEntity.addComponent(rendererComponent);

		// Update the collision map
		updateCollisionMap(tilesetDTO, tileMap);

		return tilemapEntity;
	}

	// Combine collision tiles into one list
	private void updateCollisionMap(RoomTileset tilesetDTO, int[][] mapLayout) {

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

	private int[][] getMapLayout(RoomLayer layerDTO) {
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

	private void processZones(RoomLayer zoneLayer, Scene scene) {

		List<Vector2D> enemySpawns = new ArrayList<>();

		int height = zoneLayer.height;
		int width = zoneLayer.width;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int data = zoneLayer.data.get(j * width + i) - 1;

				Vector2D worldPos = new Vector2D(i * GameConstants.TILE_SIZE, j * GameConstants.TILE_SIZE);

				switch (data) {
					case 0: //Enemy spawning tile
						enemySpawns.add(worldPos);
						break;
					case 1: //North entrance
						roomScene.getEntrances()[0] = worldPos;
						break;
					case 2: //East entrance
						roomScene.getEntrances()[1] = worldPos;
						break;
					case 3: //South entrance
						roomScene.getEntrances()[2] = worldPos;
						break;
					case 4: //West entrance
						roomScene.getEntrances()[3] = worldPos;
						break;
				}
			}
		}

		roomScene.setEnemySpawnPoints(enemySpawns);

		IEnemyFactory enemyFactory = ServiceLoader.load(IEnemyFactory.class).findFirst().orElse(null);

		if (enemyFactory != null && !enemySpawns.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				Vector2D point = enemySpawns.get((int) (Math.random() * enemySpawns.size()));

				Entity enemy = enemyFactory.create(point, 100, 5, 3);
				scene.addEntity(enemy);
			}
		}
	}
}