package dk.sdu.sem.commontilemap;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component for storing tilemap data without rendering dependencies.
 */
public class TilemapComponent implements IComponent {
	private String tilesetId;        // Tileset/palette name as a string identifier
	private int[][] tileIndices;     // 2D array of tile indices
	private int tileSize;            // Size of each tile
	private boolean isVisible = true;

	/**
	 * Creates an empty tilemap component.
	 */
	public TilemapComponent() {
	}

	/**
	 * Creates a tilemap component with specified parameters.
	 *
	 * @param tilesetId The identifier for the tileset/palette
	 * @param tileIndices 2D array of tile indices
	 * @param tileSize Size of each tile
	 */
	public TilemapComponent(String tilesetId, int[][] tileIndices, int tileSize) {
		this.tilesetId = tilesetId;
		this.tileIndices = tileIndices;
		this.tileSize = tileSize;
	}

	/**
	 * Gets the tileset identifier.
	 */
	public String getTilesetId() {
		return tilesetId;
	}

	/**
	 * Sets the tileset identifier.
	 */
	public void setTilesetId(String tilesetId) {
		this.tilesetId = tilesetId;
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