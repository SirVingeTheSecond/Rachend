package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commontilemap.TilemapComponent;

/**
 * Component that adds collision data to a tilemap.
 */
public class TilemapColliderComponent extends ColliderComponent {
	/**
	 * Creates a new tilemap collider component.
	 *
	 * @param entity The entity this collider is attached to
	 * @param collisionFlags 2D array where 1 indicates a solid tile, 0 indicates a passable tile
	 * @param tileSize The size of each tile in world units
	 */
	public TilemapColliderComponent(Entity entity, int[][] collisionFlags, int tileSize) {
		super(entity, new GridShape(collisionFlags, tileSize));
		setLayer(PhysicsLayer.OBSTACLE); // Default layer for tilemaps
	}

	/**
	 * Creates a new tilemap collider component from an existing TilemapComponent.
	 *
	 * @param entity The entity this collider is attached to
	 * @param tilemapComponent The tilemap component to derive settings from
	 * @param collisionFlags 2D array where 1 indicates a solid tile, 0 indicates a passable tile
	 */
	public TilemapColliderComponent(Entity entity, TilemapComponent tilemapComponent, int[][] collisionFlags) {
		super(entity, new GridShape(collisionFlags, tilemapComponent.getTileSize()));
		setLayer(PhysicsLayer.OBSTACLE); // Default layer for tilemaps
	}

	/**
	 * Gets the grid shape used by this tilemap collider.
	 *
	 * @return The grid shape
	 */
	public GridShape getGridShape() {
		return (GridShape) getShape();
	}

	/**
	 * Gets the collision flags grid.
	 *
	 * @return The 2D array of collision flags
	 */
	public int[][] getCollisionFlags() {
		return getGridShape().getCollisionFlags();
	}

	/**
	 * Checks if a tile is solid at the specified coordinates.
	 *
	 * @param x The tile X coordinate
	 * @param y The tile Y coordinate
	 * @return True if the tile is solid, false if it's passable
	 */
	public boolean isSolid(int x, int y) {
		return getGridShape().isSolid(x, y);
	}

	/**
	 * Sets whether a tile is solid.
	 *
	 * @param x The tile X coordinate
	 * @param y The tile Y coordinate
	 * @param solid True to make the tile solid, false to make it passable
	 */
	public void setSolid(int x, int y, boolean solid) {
		getGridShape().setSolid(x, y, solid);
	}

	/**
	 * Gets the tile size.
	 *
	 * @return The tile size in world units
	 */
	public int getTileSize() {
		return getGridShape().getTileSize();
	}

	/**
	 * Gets the width of the tilemap in tiles.
	 *
	 * @return The width in tiles
	 */
	public int getWidth() {
		return getGridShape().getGridWidth();
	}

	/**
	 * Gets the height of the tilemap in tiles.
	 *
	 * @return The height in tiles
	 */
	public int getHeight() {
		return getGridShape().getGridHeight();
	}
}