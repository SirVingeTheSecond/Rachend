package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.CollisionOptions;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.debug.DebugDrawingManager;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Debug wrapper for ICollisionSPI that visualizes raycasts
 */
public class DebugCollisionService implements ICollisionSPI {
	private static final Logging LOGGER = Logging.createLogger("DebugCollisionService", LoggingLevel.DEBUG);

	private final ICollisionSPI delegate;
	private final DebugDrawingManager debugDrawing;
	private boolean debugEnabled = true;

	public DebugCollisionService(ICollisionSPI delegate) {
		this.delegate = delegate;
		this.debugDrawing = DebugDrawingManager.getInstance();
		LOGGER.debug("DebugCollisionService initialized with delegate: " + delegate.getClass().getName());
	}

	public void setDebugEnabled(boolean enabled) {
		this.debugEnabled = enabled;
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	/**
	 * Casts a ray and visualizes it for debugging
	 */
	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		RaycastHit hit = delegate.raycast(origin, direction, maxDistance);

		if (debugEnabled) {
			visualizeRaycast(origin, direction, maxDistance, hit);
		}

		return hit;
	}

	/**
	 * Casts a ray against specific layer and visualizes it for debugging
	 */
	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		RaycastHit hit = delegate.raycast(origin, direction, maxDistance, layer);

		if (debugEnabled) {
			visualizeRaycast(origin, direction, maxDistance, hit);
		}

		return hit;
	}

	/**
	 * Casts a ray against list of layers and visualizes it for debugging
	 */
	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		RaycastHit hit = delegate.raycast(origin, direction, maxDistance, layers);

		if (debugEnabled) {
			visualizeRaycastWithLayers(origin, direction, maxDistance, hit, layers);
		}

		logRaycastResult(origin, direction, maxDistance, layers, hit);

		return hit;
	}

	/**
	 * Casts a ray and returns all hits along the ray path, sorted by distance.
	 */
	@Override
	public List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance) {
		List<RaycastHit> hits = delegate.raycastAll(origin, direction, maxDistance);

		if (debugEnabled && !hits.isEmpty()) {
			// Visualize all hits
			for (RaycastHit hit : hits) {
				visualizeRaycast(origin, direction, hit.getDistance(), hit);
			}
		} else if (debugEnabled) {
			// No hits, visualize the full ray
			visualizeRaycast(origin, direction, maxDistance, RaycastHit.noHit());
		}

		return hits;
	}

	/**
	 * Casts a ray against all colliders in specific layers and returns all hits.
	 */
	@Override
	public List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		List<RaycastHit> hits = delegate.raycastAll(origin, direction, maxDistance, layers);

		if (debugEnabled && !hits.isEmpty()) {
			// Visualize all hits
			for (RaycastHit hit : hits) {
				visualizeRaycastWithLayers(origin, direction, hit.getDistance(), hit, layers);
			}
		} else if (debugEnabled) {
			// No hits, visualize the full ray
			visualizeRaycastWithLayers(origin, direction, maxDistance, RaycastHit.noHit(), layers);
		}

		return hits;
	}

	/**
	 * Non-allocating variant of raycast. Fills the provided hitInfo object.
	 */
	@Override
	public boolean raycast(Vector2D origin, Vector2D direction, float maxDistance, RaycastHit hitInfo) {
		boolean hit = delegate.raycast(origin, direction, maxDistance, hitInfo);

		if (debugEnabled) {
			visualizeRaycast(origin, direction, hitInfo.isHit() ? hitInfo.getDistance() : maxDistance, hitInfo);
		}

		return hit;
	}

	/**
	 * Checks if a point is inside any collider.
	 */
	@Override
	public boolean isPointInCollider(Vector2D point) {
		return delegate.isPointInCollider(point);
	}

	/**
	 * Checks if a point is inside a specific collider.
	 */
	@Override
	public boolean isPointInCollider(Vector2D point, Entity entity) {
		return delegate.isPointInCollider(point, entity);
	}

	/**
	 * Overlap circle with debug visualization
	 */
	@Override
	public List<Entity> overlapCircle(Vector2D center, float radius) {
		List<Entity> results = delegate.overlapCircle(center, radius);

		if (debugEnabled) {
			// Use different alpha based on whether anything was found
			double alpha = results.isEmpty() ? 0.2 : 0.4;
			Color circleColor = results.isEmpty() ?
					Color.GRAY.deriveColor(0, 1, 1, alpha) :
					Color.ORANGE.deriveColor(0, 1, 1, alpha);

			debugDrawing.drawCircle(center, radius, circleColor, 0.01f);
		}

		return results;
	}

	/**
	 * Overlap circle with debug visualization for a specific layer
	 */
	@Override
	public List<Entity> overlapCircle(Vector2D center, float radius, PhysicsLayer layer) {
		List<Entity> results = delegate.overlapCircle(center, radius, layer);

		if (debugEnabled) {
			// Use different alpha based on whether anything was found
			double alpha = results.isEmpty() ? 0.2 : 0.4;
			Color circleColor = results.isEmpty() ?
					Color.GRAY.deriveColor(0, 1, 1, alpha) :
					Color.ORANGE.deriveColor(0, 1, 1, alpha);

			debugDrawing.drawCircle(center, radius, circleColor, 0.01f);
		}

		return results;
	}

	/**
	 * Checks if a position is valid with debug visualization
	 */
	@Override
	public boolean isPositionValid(Entity entity, Vector2D proposedPosition, CollisionOptions options) {
		boolean isValid = delegate.isPositionValid(entity, proposedPosition, options);

		if (debugEnabled) {
			Color posColor = isValid ?
					Color.GREEN.deriveColor(0, 1, 1, 0.6) :
					Color.RED.deriveColor(0, 1, 1, 0.6);
			debugDrawing.drawCircle(proposedPosition, 5, posColor, 0.01f);
		}

		return isValid;
	}

	/**
	 * Backward compatibility method for isPositionValid
	 */
	@Override
	public boolean isPositionValid(Entity entity, Vector2D proposedPosition) {
		return isPositionValid(entity, proposedPosition, CollisionOptions.preventAll(false));
	}

	/**
	 * Gets all entities that overlap a box.
	 */
	@Override
	public List<Entity> overlapBox(Vector2D center, float width, float height) {
		return delegate.overlapBox(center, width, height);
	}

	/**
	 * Gets all entities that overlap a box in a specific layer.
	 */
	@Override
	public List<Entity> overlapBox(Vector2D center, float width, float height, PhysicsLayer layer) {
		List<Entity> results = delegate.overlapBox(center, width, height, layer);

		if (debugEnabled) {
			// Visualize box using multiple lines since we don't have drawRect
			float halfWidth = width / 2;
			float halfHeight = height / 2;

			// Calculate corners
			Vector2D topLeft = new Vector2D(center.x() - halfWidth, center.y() - halfHeight);
			Vector2D topRight = new Vector2D(center.x() + halfWidth, center.y() - halfHeight);
			Vector2D bottomLeft = new Vector2D(center.x() - halfWidth, center.y() + halfHeight);
			Vector2D bottomRight = new Vector2D(center.x() + halfWidth, center.y() + halfHeight);

			// Use different alpha based on whether anything was found
			double alpha = results.isEmpty() ? 0.2 : 0.4;
			Color boxColor = results.isEmpty() ?
					Color.GRAY.deriveColor(0, 1, 1, alpha) :
					Color.ORANGE.deriveColor(0, 1, 1, alpha);

			// Draw the four sides of the box
			float duration = 0.1f;
			debugDrawing.drawLine(topLeft, topRight, boxColor, duration);
			debugDrawing.drawLine(topRight, bottomRight, boxColor, duration);
			debugDrawing.drawLine(bottomRight, bottomLeft, boxColor, duration);
			debugDrawing.drawLine(bottomLeft, topLeft, boxColor, duration);
		}

		return results;
	}

	/**
	 * Marks an entity for cleanup in the collision system.
	 */
	@Override
	public void cleanupEntity(Entity entity) {
		delegate.cleanupEntity(entity);
	}

	// Helper method to visualize a raycast
	private void visualizeRaycast(Vector2D origin, Vector2D direction, float maxDistance, RaycastHit hit) {
		// Normalize direction to ensure consistent visualization
		Vector2D normalizedDir = direction.normalize();

		// Calculate actual ray length based on hit results
		float rayLength = hit.isHit() ? hit.getDistance() : maxDistance;
		Vector2D scaledDir = normalizedDir.scale(rayLength);

		// Choose color based on hit result
		Color rayColor = hit.isHit() ? Color.GREEN : Color.RED;

		// Draw the ray
		debugDrawing.drawRay(origin, scaledDir, rayColor, 0.05f);

		// If hit, draw the normal vector at hit point
		if (hit.isHit() && hit.getNormal() != null && hit.getPoint() != null) {
			debugDrawing.drawRay(hit.getPoint(), hit.getNormal().scale(10), Color.CYAN, 0.01f);
		}
	}

	// Helper method to visualize a layered raycast with more detailed coloring
	private void visualizeRaycastWithLayers(Vector2D origin, Vector2D direction, float maxDistance,
											RaycastHit hit, List<PhysicsLayer> layers) {
		// Normalize direction
		Vector2D normalizedDir = direction.normalize();

		// Calculate ray length
		float rayLength = hit.isHit() ? hit.getDistance() : maxDistance;
		Vector2D scaledDir = normalizedDir.scale(rayLength);

		// Base color
		Color rayColor = Color.YELLOW;

		if (hit.isHit()) {
			PhysicsLayer hitLayer = null;
			if (hit.getCollider() != null) {
				hitLayer = hit.getCollider().getLayer();
			}

			// Set color based on what was hit
			if (hitLayer == PhysicsLayer.OBSTACLE) {
				rayColor = Color.RED;
			} else if (hitLayer == PhysicsLayer.PLAYER) {
				rayColor = Color.GREEN;
			} else if (hitLayer == PhysicsLayer.ENEMY) {
				rayColor = Color.ORANGE;
			} else {
				rayColor = Color.WHITE;
			}
		}

		// Draw the ray
		debugDrawing.drawRay(origin, scaledDir, rayColor, 0.01f);

		if (hit.isHit()) {
			// Draw a small circle at hit point
			debugDrawing.drawCircle(hit.getPoint(), 3, rayColor.brighter(), 0.01f);

			// Draw normal if available
			if (hit.getNormal() != null) {
				debugDrawing.drawRay(hit.getPoint(), hit.getNormal().scale(10), Color.CYAN, 0.01f);
			}
		}
	}

	// Log raycast results for debugging
	private void logRaycastResult(Vector2D origin, Vector2D direction, float maxDistance,
								  List<PhysicsLayer> layers, RaycastHit hit) {
		if (!debugEnabled) return;

		StringBuilder layerStr = new StringBuilder("[");
		for (int i = 0; i < layers.size(); i++) {
			layerStr.append(layers.get(i));
			if (i < layers.size() - 1) layerStr.append(", ");
		}
		layerStr.append("]");

		LOGGER.debug("Raycast: origin=" + origin + ", dir=" + direction +
				", dist=" + maxDistance + ", layers=" + layerStr);
		LOGGER.debug("Result: hit=" + hit.isHit() +
				", entity=" + (hit.getEntity() != null ? hit.getEntity().getID() : "null") +
				", distance=" + hit.getDistance());
	}
}