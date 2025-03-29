package dk.sdu.sem.gamesystem.factories;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

/**
 * Factory for creating tilemap entities.
 */
public class TilemapFactory implements IEntityFactory {
	@Override
	public Entity create() {
		// Create the tilemap entity
		Entity tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Generate a map layout
		int[][] tileMap = createMapLayout();

		// Create tilemap component - using the exact name registered in GameAssetProvider
		TilemapComponent tilemapComponent = new TilemapComponent(
			"floor",  // The exact name used in Assets.createSpriteSheet()
			tileMap,  // Tile indices
			GameConstants.TILE_SIZE  // Tile size
		);
		tilemapComponent.setRenderLayer(GameConstants.LAYER_FLOOR);

		tilemapEntity.addComponent(tilemapComponent);

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
			map[x][0] = 5 + (x % 2);               // Top wall
			map[x][1] = 12 + (x % 2);
			map[x][mapHeight-1] = 12 + (x % 2);     // Bottom wall
			map[x][mapHeight-2] = 5 + (x % 2);
		}

		for (int y = 0; y < mapHeight; y++) {
			map[0][y] = 1;               // Left wall
			map[mapWidth-1][y] = 1;      // Right wall
		}

		return map;
	}
}