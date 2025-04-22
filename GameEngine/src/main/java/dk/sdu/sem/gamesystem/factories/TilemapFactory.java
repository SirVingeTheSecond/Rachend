package dk.sdu.sem.gamesystem.factories;

import dk.sdu.sem.collision.IColliderFactory;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TilemapRendererComponent;

import java.util.Optional;
import java.util.ServiceLoader;

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

		// Create tilemap component (registered in GameAssetProvider!)
		TilemapComponent tilemapComponent = new TilemapComponent(
			"floor", // The registered name.
			tileMap,  // Tile indices
			GameConstants.TILE_SIZE  // Tile size
		);
		tilemapEntity.addComponent(tilemapComponent);

		// Add a renderer component that references the tilemap data
		TilemapRendererComponent rendererComponent = new TilemapRendererComponent(tilemapComponent);
		rendererComponent.setRenderLayer(GameConstants.LAYER_FLOOR);
		tilemapEntity.addComponent(rendererComponent);

		// Add collision component
		addCollisionToTilemap(tilemapEntity, tileMap);

		return tilemapEntity;
	}

	/**
	 * Adds a collision component to the tilemap entity if the collision module is available.
	 *
	 * @param tilemapEntity The tilemap entity
	 * @param tileMap The tile map data
	 */
	private void addCollisionToTilemap(Entity tilemapEntity, int[][] tileMap) {
		int[][] collisionFlags = createCollisionFlags(tileMap);

		// Direct ServiceLoader lookup
		Optional<IColliderFactory> optionalFactory = ServiceLoader.load(IColliderFactory.class).findFirst();

		if (optionalFactory.isPresent()) {
			IColliderFactory factory = optionalFactory.get();

			TilemapColliderComponent collider = factory.addTilemapCollider(
				tilemapEntity, collisionFlags, PhysicsLayer.OBSTACLE
			);

			if (collider != null) {
				System.out.println("Added collision data to tilemap");
			} else {
				System.err.println("Failed to add collision data to tilemap");
			}
		} else {
			System.out.println("No collision support available for tilemap");
		}
	}

	/**
	 * Creates collision flags for a tilemap with solid borders.
	 *
	 * @param tileMap The tile map data
	 * @return A 2D array of collision flags (1 = solid, 0 = passable)
	 */
	private int[][] createCollisionFlags(int[][] tileMap) {
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

		return collisionFlags;
	}

	private int[][] createMapLayout() {
		// Create a map that's 25x19 tiles, fits the game window for now.
		int mapWidth = 25;
		int mapHeight = 19;
		int[][] map = new int[mapWidth][mapHeight];

		// Fill with floor tiles
		int[] floorTiles = {7, 8, 14, 15};
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				map[x][y] = floorTiles[(x + y) % floorTiles.length];
			}
		}

		// Add walls around the edges
		for (int x = 0; x < mapWidth; x++) {
			map[x][0] = 24;               // Top wall
			map[x][mapHeight-1] = 24;     // Bottom wall
		}

		for (int y = 0; y < mapHeight; y++) {
			map[0][y] = 1;               // Left wall
			map[mapWidth-1][y] = 1;      // Right wall
		}

		return map;
	}
}