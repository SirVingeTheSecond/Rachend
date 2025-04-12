package dk.sdu.sem.collision.components;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
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

	@Override
	public AABB getBounds() {
		Vector2D worldPosition = getWorldPosition();
		GridShape gridShape = getGridShape();
		int tileSize = getTileSize();

		return new AABB(
			worldPosition.x(),
			worldPosition.y(),
			worldPosition.x() + (gridShape.getGridWidth() * tileSize),
			worldPosition.y() + (gridShape.getGridHeight() * tileSize)
		);
	}

	@Override
	public ContactPoint collidesWith(ColliderComponent other) {
		ICollisionSPI collisionSystem = ServiceLocator.getService(ICollisionSPI.class);
		if (collisionSystem != null) {
			return collisionSystem.getCollisionInfo(this, other);
		}
		return null;
	}

	@Override
	public RaycastHit raycast(Ray ray, float maxDistance) {
		GridShape grid = getGridShape();
		Vector2D worldPos = getWorldPosition();
		int tileSize = getTileSize();
		int maxSteps = 100; // Prevent infinite loops

		// Calculate ray in tilemap space
		Vector2D relativeOrigin = ray.getOrigin().subtract(worldPos);
		Vector2D direction = ray.getDirection();

		// Current tile indices
		int tileX = (int)(relativeOrigin.x() / tileSize);
		int tileY = (int)(relativeOrigin.y() / tileSize);

		// Calculate step direction
		int stepX = direction.x() < 0 ? -1 : 1;
		int stepY = direction.y() < 0 ? -1 : 1;

		// Calculate delta distance (distance to next cell boundary)
		float deltaDistX = Math.abs(direction.x()) < 0.0001f ? Float.MAX_VALUE :
			Math.abs(tileSize / direction.x());
		float deltaDistY = Math.abs(direction.y()) < 0.0001f ? Float.MAX_VALUE :
			Math.abs(tileSize / direction.y());

		// Calculate initial side distance
		float sideDistX;
		if (direction.x() < 0) {
			sideDistX = (relativeOrigin.x() - tileX * tileSize) / direction.x();
		} else {
			sideDistX = ((tileX + 1) * tileSize - relativeOrigin.x()) / direction.x();
		}

		float sideDistY;
		if (direction.y() < 0) {
			sideDistY = (relativeOrigin.y() - tileY * tileSize) / direction.y();
		} else {
			sideDistY = ((tileY + 1) * tileSize - relativeOrigin.y()) / direction.y();
		}

		// Normalize side distances
		sideDistX = Math.abs(sideDistX);
		sideDistY = Math.abs(sideDistY);

		// Perform DDA (Digital Differential Analysis)
		boolean hit = false;
		boolean hitX = false;
		float distance = 0f;

		for (int steps = 0; steps < maxSteps && !hit && distance < maxDistance; steps++) {
			// Move to next cell
			if (sideDistX < sideDistY) {
				distance = sideDistX;
				sideDistX += deltaDistX;
				tileX += stepX;
				hitX = true;
			} else {
				distance = sideDistY;
				sideDistY += deltaDistY;
				tileY += stepY;
				hitX = false;
			}

			// Check if we hit a solid tile
			if (tileX >= 0 && tileX < grid.getGridWidth() &&
				tileY >= 0 && tileY < grid.getGridHeight() &&
				grid.isSolid(tileX, tileY)) {
				hit = true;
			}
		}

		if (hit) {
			// Calculate hit point
			Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(distance));

			// Calculate hit normal based on which side was hit
			Vector2D hitNormal = hitX ?
				new Vector2D(direction.x() < 0 ? 1 : -1, 0) :
				new Vector2D(0, direction.y() < 0 ? 1 : -1);

			return new RaycastHit(true, entity, hitPoint, hitNormal, distance, this);
		}

		return RaycastHit.noHit();
	}

	@Override
	public Vector2D closestPoint(Vector2D point) {
		Vector2D worldPos = getWorldPosition();
		GridShape grid = getGridShape();
		int tileSize = getTileSize();

		// Convert to tile coordinates
		int tileX = (int)((point.x() - worldPos.x()) / tileSize);
		int tileY = (int)((point.y() - worldPos.y()) / tileSize);

		// Clamp to grid bounds
		tileX = Math.max(0, Math.min(tileX, grid.getGridWidth() - 1));
		tileY = Math.max(0, Math.min(tileY, grid.getGridHeight() - 1));

		// If the tile is not solid, the point is already the closest
		if (!grid.isSolid(tileX, tileY)) {
			// For non-solid tiles, we would need to search surrounding solid tiles
			// This is a simplified implementation for brevity
			return point;
		}

		// For solid tiles, find closest edge
		float tileLeft = worldPos.x() + (tileX * tileSize);
		float tileRight = tileLeft + tileSize;
		float tileTop = worldPos.y() + (tileY * tileSize);
		float tileBottom = tileTop + tileSize;

		// Find distances to each edge
		float distLeft = Math.abs(point.x() - tileLeft);
		float distRight = Math.abs(point.x() - tileRight);
		float distTop = Math.abs(point.y() - tileTop);
		float distBottom = Math.abs(point.y() - tileBottom);

		// Return closest edge point
		float minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

		if (minDist == distLeft) return new Vector2D(tileLeft, point.y());
		if (minDist == distRight) return new Vector2D(tileRight, point.y());
		if (minDist == distTop) return new Vector2D(point.x(), tileTop);
		return new Vector2D(point.x(), tileBottom);
	}
}