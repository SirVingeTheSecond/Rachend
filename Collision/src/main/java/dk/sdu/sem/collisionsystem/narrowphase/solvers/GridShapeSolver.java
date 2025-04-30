package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Handles collisions between shapes and a grid shape (tilemap).
 */
public class GridShapeSolver {
	private final GJKSolver solver = new GJKSolver();

	/**
	 * Tests collision between a circle and a grid shape.
	 *
	 * @param circle The circle shape
	 * @param circlePos The circle position
	 * @param grid The grid shape
	 * @param gridPos The grid position
	 * @return ContactPoint if collision detected, null otherwise
	 */
	public ContactPoint solveCircleGrid(CircleShape circle, Vector2D circlePos,
										GridShape grid, Vector2D gridPos) {
		float radius = circle.getRadius();
		int tileSize = grid.getTileSize();

		// Determine tile range to check based on circle bounds
		Vector2D relativePosA = circlePos.subtract(gridPos);
		int minTileX = (int) Math.floor((relativePosA.x() - radius) / tileSize);
		int maxTileX = (int) Math.ceil((relativePosA.x() + radius) / tileSize);
		int minTileY = (int) Math.floor((relativePosA.y() - radius) / tileSize);
		int maxTileY = (int) Math.ceil((relativePosA.y() + radius) / tileSize);

		// Clamp to map bounds
		minTileX = Math.max(0, minTileX);
		minTileY = Math.max(0, minTileY);
		maxTileX = Math.min(grid.getGridWidth() - 1, maxTileX);
		maxTileY = Math.min(grid.getGridHeight() - 1, maxTileY);

		// Find the first collision
		ContactPoint firstContact = null;
		float closestDistance = Float.MAX_VALUE;

		// Check each tile in range
		for (int y = minTileY; y <= maxTileY; y++) {
			for (int x = minTileX; x <= maxTileX; x++) {
				// Skip non-solid tiles
				if (!grid.isSolid(x, y)) {
					continue;
				}

				// Create temporary box for the tile
				Vector2D tilePos = grid.tileToWorld(x, y, gridPos);
				BoxShape tileBox = new BoxShape(tileSize, tileSize);

				// Check circle-box collision
				ContactPoint contact = solver.solve(circle, circlePos, tileBox, tilePos);

				if (contact != null) {
					// Store contact with smallest distance
					float distance = circlePos.distance(contact.getPoint());
					if (firstContact == null || distance < closestDistance) {
						firstContact = contact;
						closestDistance = distance;
					}
				}
			}
		}

		return firstContact;
	}

	/**
	 * Tests collision between a box and a grid shape.
	 *
	 * @param box The box shape
	 * @param boxPos The box position
	 * @param grid The grid shape
	 * @param gridPos The grid position
	 * @return ContactPoint if collision detected, null otherwise
	 */
	public ContactPoint solveBoxGrid(BoxShape box, Vector2D boxPos,
									 GridShape grid, Vector2D gridPos) {
		float width = box.getWidth();
		float height = box.getHeight();
		int tileSize = grid.getTileSize();

		// Determine tile range to check based on box bounds
		Vector2D relativePosA = boxPos.subtract(gridPos);
		int minTileX = (int) Math.floor(relativePosA.x() / tileSize);
		int maxTileX = (int) Math.ceil((relativePosA.x() + width) / tileSize);
		int minTileY = (int) Math.floor(relativePosA.y() / tileSize);
		int maxTileY = (int) Math.ceil((relativePosA.y() + height) / tileSize);

		// Clamp to map bounds
		minTileX = Math.max(0, minTileX);
		minTileY = Math.max(0, minTileY);
		maxTileX = Math.min(grid.getGridWidth() - 1, maxTileX);
		maxTileY = Math.min(grid.getGridHeight() - 1, maxTileY);

		// Find the first collision
		ContactPoint firstContact = null;
		float closestDistance = Float.MAX_VALUE;

		// Check each tile in range
		for (int y = minTileY; y <= maxTileY; y++) {
			for (int x = minTileX; x <= maxTileX; x++) {
				// Skip non-solid tiles
				if (!grid.isSolid(x, y)) {
					continue;
				}

				// Create temporary box for the tile
				Vector2D tilePos = grid.tileToWorld(x, y, gridPos);
				BoxShape tileBox = new BoxShape(tileSize, tileSize);

				// Check box-box collision
				ContactPoint contact = solver.solve(box, boxPos, tileBox, tilePos);

				if (contact != null) {
					// Store contact with smallest distance
					float distance = boxPos.distance(contact.getPoint());
					if (firstContact == null || distance < closestDistance) {
						firstContact = contact;
						closestDistance = distance;
					}
				}
			}
		}

		return firstContact;
	}
}