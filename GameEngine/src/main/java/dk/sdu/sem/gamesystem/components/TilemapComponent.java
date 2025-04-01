package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

/**
 * Component for managing tile maps.
 */
public class TilemapComponent implements IComponent {
	private String palette;        // Tileset name
	private int[][] tileIndices;   // 2D array of tile indices
	private int tileSize;          // Size of each tile
	private int renderLayer = 10;  // Default render layer
	private boolean isVisible = true;
	private SpriteMap spriteMap;   // Cached sprite map

	/**
	 * Creates an empty tilemap component.
	 */
	public TilemapComponent() {
	}

	/**
	 * Creates a tilemap component with specified parameters.
	 *
	 * @param palette The name of the tileset/palette
	 * @param tileIndices 2D array of tile indices
	 * @param tileSize Size of each tile
	 */
	public TilemapComponent(String palette, int[][] tileIndices, int tileSize) {
		this.palette = palette;
		this.tileIndices = tileIndices;
		this.tileSize = tileSize;
		loadSpriteMap();
	}

	/**
	 * Loads the sprite map from the palette name.
	 */
	private void loadSpriteMap() {
		if (palette != null) {
			try {
				this.spriteMap = AssetFacade.createSpriteMap(palette)
					.withAutoDetectTileSize()
					.load();
			} catch (Exception e) {
				System.err.println("Failed to load sprite map for palette: " + palette);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the palette name.
	 */
	public String getPalette() {
		return palette;
	}

	/**
	 * Sets the palette name and loads the corresponding sprite map.
	 */
	public void setPalette(String palette) {
		this.palette = palette;
		loadSpriteMap();
	}

	/**
	 * Gets the sprite map.
	 */
	public SpriteMap getSpriteMap() {
		if (spriteMap == null) {
			loadSpriteMap();
		}
		return spriteMap;
	}

	/**
	 * Gets the tile indices.
	 */
	public int[][] getTileIndices() {
		return tileIndices;
	}

	/**
	 * Sets the tile indices.
	 */
	public void setTileIndices(int[][] tileIndices) {
		this.tileIndices = tileIndices;
	}

	/**
	 * Gets the tile size.
	 */
	public int getTileSize() {
		return tileSize;
	}

	/**
	 * Sets the tile size.
	 */
	public void setTileSize(int tileSize) {
		this.tileSize = tileSize;
	}

	/**
	 * Gets the render layer.
	 */
	public int getRenderLayer() {
		return renderLayer;
	}

	/**
	 * Sets the render layer.
	 */
	public void setRenderLayer(int renderLayer) {
		this.renderLayer = renderLayer;
	}

	/**
	 * Checks if the tilemap is visible.
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Sets whether the tilemap is visible.
	 */
	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	/**
	 * Sets a tile at specific coordinates.
	 */
	public void setTile(int x, int y, int tileId) {
		if (tileIndices != null && x >= 0 && x < tileIndices.length && y >= 0 && y < tileIndices[0].length) {
			tileIndices[x][y] = tileId;
		}
	}

	/**
	 * Gets a tile at specific coordinates.
	 */
	public int getTile(int x, int y) {
		if (tileIndices != null && x >= 0 && x < tileIndices.length && y >= 0 && y < tileIndices[0].length) {
			return tileIndices[x][y];
		}
		return -1; // Invalid tile
	}
}