package dk.sdu.sem.levelsystem.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.TilemapColliderComponent;
import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.levelsystem.parsing.dto.LayerDTO;
import dk.sdu.sem.levelsystem.parsing.dto.LevelDataDTO;
import dk.sdu.sem.levelsystem.parsing.dto.TilesetDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LevelParser implements ILevelSPI {
	public void createLevelFromFile(File levelData) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			LevelDataDTO dto = mapper.readValue(levelData, LevelDataDTO.class);

			List<String> tileSets = createTileSets(dto);

			int[] cutPoints = new int[dto.tilesets.size()];
			//Fill the list, first being 0
			for (int i = 0; i < dto.tilesets.size(); i++) {
				if (i == 0)
					cutPoints[i] = 0;
				else
					cutPoints[i] = cutPoints[i - 1] + dto.tilesets.get(i - 1).tileCount;

				System.out.println(cutPoints[i]);
			}

			for (LayerDTO layer : dto.layers) {

				for (int i = 0; i < dto.tilesets.size(); i++) {
					int finalI = i;
					List<Integer> psdLayer = layer.data.stream()
							.map(d -> (d > cutPoints[finalI] && (finalI == cutPoints.length - 1 || d < cutPoints[finalI + 1])) ? d - cutPoints[finalI] : 0)
							.toList();

					if (psdLayer.stream().anyMatch(d -> d != 0)) {

						LayerDTO layerDTO = new LayerDTO();
						layerDTO.data = psdLayer;
						layerDTO.name = layer.name;
						layerDTO.width = layer.width;
						layerDTO.height = layer.height;

						Entity tileMapEntity = createTileMapEntity(layerDTO, tileSets.get(i), dto.tilesets.get(i));
						Scene.getActiveScene().addEntity(tileMapEntity);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> createTileSets(LevelDataDTO dto) {
		List<String> tileSets = new ArrayList<>();

		for (TilesetDTO tileset : dto.tilesets) {
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

	public Entity createTileMapEntity(LayerDTO layerDTO, String tileMapName, TilesetDTO tilesetDTO) {
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

		int renderLayer = switch (layerDTO.name) {
			case "LAYER_FOREGROUND" -> GameConstants.LAYER_FOREGROUND;
			case "LAYER_MIDGROUND" -> GameConstants.LAYER_MIDGROUND;
			case "LAYER_BACKGROUND" -> GameConstants.LAYER_BACKGROUND;
			default -> GameConstants.LAYER_FLOOR;
		};

		tilemapComponent.setRenderLayer(renderLayer);

		tilemapEntity.addComponent(tilemapComponent);

		//Collisions
		int[][] collisionFlags = getCollisionLayout(tilesetDTO, tileMap);

		TilemapColliderComponent collider = new TilemapColliderComponent(collisionFlags);
		collider.setLayer(PhysicsLayer.OBSTACLE);
		tilemapEntity.addComponent(collider);

		return tilemapEntity;
	}

	public int[][] getCollisionLayout(TilesetDTO tilesetDTO, int[][] mapLayout) {

		int width = mapLayout.length;
		int height = mapLayout[0].length;
		int[][] result = new int[width][height];

		if (tilesetDTO.tiles == null)
			return result;

		List<Integer> collisionIDs = tilesetDTO.tiles.stream()
				.filter(t -> t.properties.stream()
						.anyMatch(p -> p.name.equals("collision") && (boolean)p.value))
				.map(t -> t.id).toList();


		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				result[i][j] = collisionIDs.contains(mapLayout[i][j]) ? 1 : 0;
			}
		}

		return result;
	}

	public int[][] getMapLayout(LayerDTO layerDTO) {
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

	@Override
	public void createLevel() {
		createLevelFromFile(new File("Levels/stage1/leveldata.json"));
	}
}
