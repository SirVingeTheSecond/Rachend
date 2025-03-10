package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.ICollider;
import dk.sdu.sem.collision.ICollisionShape;
import dk.sdu.sem.collision.CircleShape;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GridCollisionService implements ICollisionSPI {

	// Made this thread-safe just in case
	private final List<ICollider> colliders = new CopyOnWriteArrayList<>();

	// The cell size for the uniform grid
	private static final float CELL_SIZE = 50.0f;

	// Scheduler for fixed update at approximately 60Hz (every ~16ms) <- SHOULD THIS BE FETCHED FROM GameEngine INSTEAD?
	private final ScheduledExecutorService scheduler;

	public GridCollisionService() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		// Schedule the collision processing at 60Hz (every 16ms)
		scheduler.scheduleAtFixedRate(this::processCollisions, 0, 16, TimeUnit.MILLISECONDS);
	}

	@Override
	public void registerCollider(ICollider collider) {
		if (collider != null) {
			colliders.add(collider);
		}
	}

	@Override
	public void unregisterCollider(ICollider collider) {
		colliders.remove(collider);
	}

	/**
	 * Processes collision detection and immediately handles any detected collisions.
	 */
	@Override
	public void processCollisions() {
		// Build the spatial grid: key = "cellX, cellY", value = list of colliders in that cell
		Map<String, List<ICollider>> grid = new HashMap<>();

		// Populate grid with all colliders
		for (ICollider collider : colliders) {
			ICollisionShape shape = collider.getCollisionShape();
			if (shape instanceof CircleShape) {
				CircleShape circle = (CircleShape) shape;
				Vector2D center = circle.getCenter();
				int cellX = (int) (center.getX() / CELL_SIZE);
				int cellY = (int) (center.getY() / CELL_SIZE);
				String key = cellX + ", " + cellY;
				grid.computeIfAbsent(key, k -> new ArrayList<>()).add(collider);
			}
		}

		// For each occupied cell, check collisions within the cell and neighboring cells
		for (Map.Entry<String, List<ICollider>> entry : grid.entrySet()) {
			String[] coords = entry.getKey().split(",");
			int cellX = Integer.parseInt(coords[0]);
			int cellY = Integer.parseInt(coords[1]);
			List<ICollider> cellColliders = entry.getValue();

			// Check current cell and its eight neighbors
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					String neighborKey = (cellX + dx) + "," + (cellY + dy);
					List<ICollider> neighborColliders = grid.get(neighborKey);
					if (neighborColliders == null) continue;

					// For each pair, check collision (avoid duplicate checks)
					for (ICollider colliderA : cellColliders) {
						for (ICollider colliderB : neighborColliders) {
							if (colliderA == colliderB) continue;
							// Avoid duplicate checks by comparing entity IDs
							if (shouldCheckCollision(colliderA, colliderB)) {
								if (colliderA.getCollisionShape().intersects(colliderB.getCollisionShape())) {
									// Handle the collision between the two colliders
									resolveCollision(colliderA, colliderB);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Helper method to ensure each collider pair (a pair should be made a separate class and take a generic) is checked only once.
	 * Uses a simple ordering based on entity IDs.
	 */
	private boolean shouldCheckCollision(ICollider a, ICollider b) {
		return a.getEntity().getID().compareTo(b.getEntity().getID()) < 0;
	}

	/**
	 * Processes the collision between two colliders.
	 */
	private void resolveCollision(ICollider a, ICollider b) {
		System.out.println("Handled collision between " + a.getEntity().getID() + " and " + b.getEntity().getID());
		// TODO: Implement collision resolution (damage, effects, you name it)
	}

	/**
	 * Shuts down the fixed update scheduler when the collision service is no longer needed.
	 */
	public void shutdown() {
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdown();
		}
	}
}
