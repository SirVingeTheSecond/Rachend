package dk.sdu.sem.collision.shapes;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * A grid-based collision shape for tilemaps.
 */
public class GridShape implements ICollisionShape {
	private final int[][] collisionFlags;
	private final int tileSize;
	private final int width;
	private final int height;

	/**
	 * Creates a new grid shape for a tilemap.
	 *
	 * @param collisionFlags 2D array where 1 indicates a solid tile, 0 indicates a passable tile
	 * @param tileSize The size of each tile in world units
	 */
	public GridShape(int[][] collisionFlags, int tileSize) {
		this.collisionFlags = collisionFlags;
		this.tileSize = tileSize;
		this.width = collisionFlags.length;
		this.height = collisionFlags[0].length;
	}

	/**
	 * Gets the collision flags array.
	 */
	public int[][] getCollisionFlags() {
		return collisionFlags;
	}

	/**
	 * Gets the tile size.
	 */
	public int getTileSize() {
		return tileSize;
	}

	/**
	 * Gets the width of the grid in tiles.
	 */
	public int getGridWidth() {
		return width;
	}

	/**
	 * Gets the height of the grid in tiles.
	 */
	public int getGridHeight() {
		return height;
	}

	/**
	 * Checks if a tile is solid using coordinates.
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
	 * Sets whether a tile is solid using coordinates.
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
	 * Converts a world position to tile coordinates.
	 *
	 * @param position The world position
	 * @param origin The origin of the tilemap in world coordinates
	 * @return A Vector2D where x and y are the tile coordinates (can be fractional)
	 */
	public Vector2D worldToTile(Vector2D position, Vector2D origin) {
		Vector2D relative = position.subtract(origin);
		return new Vector2D(
			relative.x() / tileSize,
			relative.y() / tileSize
		);
	}

	/**
	 * Converts tile coordinates to a world position.
	 *
	 * @param tileX The tile X coordinate
	 * @param tileY The tile Y coordinate
	 * @param origin The origin of the tilemap in world coordinates
	 * @return The world position of the tile's top-left corner
	 */
	public Vector2D tileToWorld(int tileX, int tileY, Vector2D origin) {
		return new Vector2D(
			origin.x() + (tileX * tileSize),
			origin.y() + (tileY * tileSize)
		);
	}

	@Override
	public boolean intersects(ICollisionShape other) {
		// This is implemented in dedicated collision solvers for better performance
		// Tilemap collisions are typically handled by specialized code
		return true;
	}

	@Override
	public boolean contains(Vector2D point) {
		// Check if the point is within the bounds of the grid
		if (point.x() < 0 || point.y() < 0 ||
			point.x() >= width * tileSize ||
			point.y() >= height * tileSize) {
			return false;
		}

		// Determine which tile the point is in
		int tileX = (int)(point.x() / tileSize);
		int tileY = (int)(point.y() / tileSize);

		// Check if the tile is solid
		return isSolid(tileX, tileY);
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(0, 0, width * tileSize, height * tileSize);
	}
}