package dk.sdu.sem.gamesystem.factories;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TileMapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.assets.AssetManager;
import dk.sdu.sem.gamesystem.assets.SpriteMapReference;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.rendering.TileSet;

public class TileMapFactory implements IEntityFactory {
	@Override
	public Entity create() {
		// Create the tilemap entity
		Entity tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Get the tileset from AssetManager
		SpriteMap spriteMap = AssetManager.getInstance().getAsset(
			new SpriteMapReference("floor_tiles"));

		if (spriteMap == null) {
			throw new IllegalStateException("Failed to load floor_tiles sprite map");
		}

		// Create tileset
		TileSet tileSet = new TileSet("floor", spriteMap);

		// Define all tiles
		tileSet.defineAllTilesFromGrid(7, 7);

		// Generate a map layout
		int[][] tileMap = createMapLayout();

		// Create tilemap component
		TileMapComponent tileMapComponent = new TileMapComponent(
			tileSet,
			tileMap,
			GameConstants.TILE_SIZE
		);
		tileMapComponent.setRenderLayer(GameConstants.LAYER_TERRAIN);

		tilemapEntity.addComponent(tileMapComponent);

		return tilemapEntity;
	}

	private int[][] createMapLayout() {
		// Create a map that's 25x19 tiles
		int mapWidth = 25;
		int mapHeight = 19;
		int[][] map = new int[mapWidth][mapHeight];

		// Fill with floor tiles (using indices 7, 8, 14, 15 as good floor tiles)
		int[] floorTiles = {7, 8, 14, 15};
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				map[x][y] = floorTiles[(x + y) % floorTiles.length];
			}
		}

		// Add walls around the edges
		for (int x = 0; x < mapWidth; x++) {
			map[x][0] = 1;               // Top wall
			map[x][mapHeight-1] = 1;     // Bottom wall
		}

		for (int y = 0; y < mapHeight; y++) {
			map[0][y] = 1;               // Left wall
			map[mapWidth-1][y] = 1;      // Right wall
		}

		return map;
	}
}