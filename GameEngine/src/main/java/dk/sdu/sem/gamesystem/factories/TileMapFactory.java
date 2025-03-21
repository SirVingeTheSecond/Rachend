package dk.sdu.sem.gamesystem.factories;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.TileMapComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.rendering.ResourceManager;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;
import dk.sdu.sem.gamesystem.rendering.TileSet;

/**
 * Factory for creating tilemap entities.
 */
public class TileMapFactory implements IEntityFactory {
	private final ResourceManager resourceManager;
	private final String tilesetPath;
	private final int renderLayer;
	private final boolean useCustomMap;
	private final int[][] customMap;

	/**
	 * Creates a TileMapFactory that loads the floor.png tileset and generates a random map.
	 */
	public TileMapFactory() {
		this("floor.png", GameConstants.LAYER_TERRAIN, false, null);
	}

	/**
	 * Creates a TileMapFactory with custom tileset path and layer settings.
	 *
	 * @param tilesetPath Path to the tileset image resource
	 * @param renderLayer Layer to render this tilemap on
	 * @param useCustomMap Whether to use a custom map layout
	 * @param customMap Custom map layout, or null to generate one
	 */
	// For useCustomMap, we could just check whether customMap is != null
	public TileMapFactory(String tilesetPath, int renderLayer, boolean useCustomMap, int[][] customMap) {
		this.resourceManager = ResourceManager.getInstance();
		this.tilesetPath = tilesetPath;
		this.renderLayer = renderLayer;
		this.useCustomMap = useCustomMap;
		this.customMap = customMap;
	}

	@Override
	public Entity create() {
		// Create the tilemap entity
		Entity tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Load the tileset
		SpriteMap tileSheet = loadTileSheet();
		TileSet tileSet = createTileSet(tileSheet);

		// Create or get the map data
		int[][] tileMap = useCustomMap ? customMap : createRandomMap(7, 7);

		// Create and add the tilemap component
		TileMapComponent tileMapComponent = new TileMapComponent(
			tileSet,
			tileMap,
			GameConstants.TILE_SIZE
		);
		tileMapComponent.setRenderLayer(renderLayer);

		tilemapEntity.addComponent(tileMapComponent);

		return tilemapEntity;
	}

	/**
	 * Loads the tileset image and creates a sprite map.
	 */
	private SpriteMap loadTileSheet() {
		// Create a sprite map from the tileset image
		String sheetName = tilesetPath.substring(0, tilesetPath.lastIndexOf('.'));
		SpriteMap sheet = resourceManager.createSpriteSheet(sheetName, tilesetPath);

		// floor.png has 7x7 tiles
		sheet.defineSpritesFromGrid(7, 7, 16, 16);

		return sheet;
	}

	/**
	 * Creates a tileset from a sprite map.
	 */
	private TileSet createTileSet(SpriteMap sheet) {
		String tilesetName = tilesetPath.substring(0, tilesetPath.lastIndexOf('.'));
		TileSet tileSet = resourceManager.createTileSet(tilesetName, tilesetName);

		tileSet.defineAllTilesFromGrid(7, 7);

		return tileSet;
	}

	/**
	 * Creates a random map layout.
	 *
	 * @param tilesetWidth Width of the tileset (in tiles)
	 * @param tilesetHeight Height of the tileset (in tiles)
	 * @return A 2D array of tile indices
	 */
	private int[][] createRandomMap(int tilesetWidth, int tilesetHeight) {
		// Create a map that's 25x19 tiles (fits well on 800x600 screen with 32px tiles)
		int mapWidth = 25;
		int mapHeight = 19;
		int[][] map = new int[mapWidth][mapHeight];

		// Total number of tiles in the tileset
		int totalTiles = tilesetWidth * tilesetHeight;

		// Fill the map with floor tiles
		int[] floorTiles = {7, 8, 14, 15};
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				// A quick way to use different but consistent floor tiles
				map[x][y] = floorTiles[(x + y) % floorTiles.length];
			}
		}

		// Add walls
		for (int x = 0; x < mapWidth; x++) {
			// Top wall
			map[x][0] = 1;
			// Bottom wall
			map[x][mapHeight-1] = 1;
		}

		for (int y = 0; y < mapHeight; y++) {
			// Left wall
			map[0][y] = 1;
			// Right wall
			map[mapWidth-1][y] = 1;
		}

		return map;
	}
}