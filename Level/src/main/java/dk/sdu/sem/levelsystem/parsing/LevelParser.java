package dk.sdu.sem.levelsystem.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LevelParser implements ILevelSPI {
	public void parse(File levelData) {

		ObjectMapper mapper = new ObjectMapper();
		try {
			LevelDataDTO dto = mapper.readValue(levelData, LevelDataDTO.class);

			mapper.writer().writeValue(System.out, dto);

			String[] split = dto.tilesets.get(0).imagePath.split("/");
			String fileName = split[split.length - 1];
			String patternName = fileName.replace(".png", "");

			//AssetFacade.createSpriteSheet(patternName, image, 16, 16);
			AssetFacade.createSpriteMap(patternName)
				.withImagePath("Levels/tilesets/" + fileName)
				.withGrid(dto.tilesets.get(0).columns, dto.tilesets.get(0).rows(), dto.tilesets.get(0).tileWidth, dto.tilesets.get(0).tileHeight)
				.load();



			for (LayerDTO layer : dto.layers) {
				Entity tileMapEntity = createTileMapEntity(layer, patternName, dto.tilesets.get(0));
				Scene.getActiveScene().addEntity(tileMapEntity);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		parse(new File("Levels\\stage1\\leveldata.json"));
	}
}
