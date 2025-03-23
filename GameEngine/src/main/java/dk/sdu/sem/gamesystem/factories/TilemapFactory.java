package dk.sdu.sem.gamesystem.factories;

import dk.sdu.sem.collision.TilemapColliderComponent;
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
		tilemapComponent.setRenderLayer(GameConstants.LAYER_TERRAIN);

		tilemapEntity.addComponent(tilemapComponent);

		// Add collision information (optional - only if Collision module is present)
		try {
			// Create collision flags array (same dimensions as the tile map)
			int[][] collisionFlags = new int[tileMap.length][tileMap[0].length];

			// Mark outer tiles as walls
			for (int x = 0; x < tileMap.length; x++) {
				collisionFlags[x][0] = 1; // Top edge
				collisionFlags[x][tileMap[0].length-1] = 1; // Bottom edge
			}

			for (int y = 0; y < tileMap[0].length; y++) {
				collisionFlags[0][y] = 1; // Left edge
				collisionFlags[tileMap.length-1][y] = 1; // Right edge
			}

			// Try to instantiate the TilemapColliderComponent
			// This will fail with ClassNotFoundException if Collision module is not present
			Class<?> colliderClass = Class.forName("dk.sdu.sem.collision.TilemapColliderComponent");
			Object collider = colliderClass.getConstructor(int[][].class).newInstance((Object) collisionFlags);

			// Add the component using reflection
			tilemapEntity.addComponent((TilemapColliderComponent) collider);

			System.out.println("Added collision data to tilemap");
		} catch (Exception e) {
			// Collision module not present or other error - tilemap works fine without collision
			System.out.println("No collision support available for tilemap");
		}

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