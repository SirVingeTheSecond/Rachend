package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component that adds collision data to a tilemap.
 */
public class TilemapColliderComponent implements IComponent {
	private int[][] collisionFlags;
	private final int width;
	private final int height;

	/**
	 * Creates a new tilemap collider component.
	 *
	 * @param collisionFlags 2D array where 1 indicates a solid tile, 0 indicates a passable tile
	 */
	public TilemapColliderComponent(int[][] collisionFlags) {
		this.collisionFlags = collisionFlags;
		this.width = collisionFlags.length;
		this.height = collisionFlags[0].length;
	}

	/**
	 * Checks if a tile is solid.
	 * Optimized boundary checking.
	 *
	 * @param x X coordinate in the tilemap
	 * @param y Y coordinate in the tilemap
	 * @return True if the tile is solid, false otherwise
	 */
	public boolean isSolid(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return true; // Out of bounds is solid
		}
		return collisionFlags[x][y] == 1;
	}

	/**
	 * Sets whether a tile is solid.
	 *
	 * @param x X coordinate in the tilemap
	 * @param y Y coordinate in the tilemap
	 * @param solid True to make the tile solid, false to make it passable
	 */
	public void setSolid(int x, int y, boolean solid) {
		if (x >= 0 && y >= 0 && x < width && y < height) {
			collisionFlags[x][y] = solid ? 1 : 0;
		}
	}

	/**
	 * Gets the collision flags array.
	 *
	 * @return The collision flags array
	 */
	public int[][] getCollisionFlags() {
		return collisionFlags;
	}

	/**
	 * Sets the collision flags array.
	 *
	 * @param collisionFlags The new collision flags array
	 */
	public void setCollisionFlags(int[][] collisionFlags) {
		this.collisionFlags = collisionFlags;
	}

	/**
	 * Gets the width of the collision grid.
	 *
	 * @return Width in tiles
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height of the collision grid.
	 *
	 * @return Height in tiles
	 */
	public int getHeight() {
		return height;
	}
}