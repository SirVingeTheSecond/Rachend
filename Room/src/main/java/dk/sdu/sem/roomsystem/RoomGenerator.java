package dk.sdu.sem.roomsystem;

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

		if (!scene.getEntities().isEmpty()) {
			ServiceLoader.load(IRoomCreatedListener.class).forEach(l -> l.onRoomCreated(roomScene));
			return roomScene;
		}

		return null;
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
		int height = zoneLayer.height;
		int width = zoneLayer.width;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int data = zoneLayer.data.get(j * width + i) - 1;

				Vector2D worldPos = new Vector2D(
					i * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
					j * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f);

				switch (data) {
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

				Zone zone = Zone.getZoneByNumVal(data);
				if (zone != null)
					roomScene.addZonePosition(zone, worldPos);

			}
		}
	}
}