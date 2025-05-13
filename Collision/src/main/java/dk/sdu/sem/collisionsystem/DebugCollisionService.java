package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.CollisionOptions;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.debug.IDebugDrawManager;

import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Debug wrapper for ICollisionSPI that visualizes raycasts, overlaps, and collision tests.
 * This implementation delegates to the base collision service while adding visual debugging.
 */
public class DebugCollisionService implements ICollisionSPI {
	private static final Logging LOGGER = Logging.createLogger("DebugCollisionService", LoggingLevel.DEBUG);

	private final ICollisionSPI delegate;
	private final IDebugDrawManager debugManager;
	private boolean debugEnabled = true;

	/**
	 * Creates a new debug collision service that wraps an existing collision service.
	 *
	 * @param delegate The base collision service to delegate operations to
	 */
	public DebugCollisionService(ICollisionSPI delegate) {
		this.delegate = delegate;

		this.debugManager = ServiceLoader.load(IDebugDrawManager.class)
			.findFirst()
			.orElse(null);

		if (this.debugManager == null) {
			LOGGER.error("Failed to get IDebugDrawManager instance - debug visualizations will be disabled");
		} else {
			LOGGER.debug("DebugCollisionService initialized with debug manager: " + debugManager.getClass().getName());
		}

		LOGGER.debug("DebugCollisionService initialized with delegate: " + delegate.getClass().getName());
	}

	/**
	 * Enables or disables debug visualization.
	 */
	public void setDebugEnabled(boolean enabled) {
		this.debugEnabled = enabled;
		LOGGER.debug("Debug collision service debug enabled: " + enabled);
	}

	/**
	 * Checks if debug visualization is enabled.
	 */
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	/**
	 * Casts a ray against all colliders in the scene and visualizes it.
	 */
	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		RaycastHit hit = delegate.raycast(origin, direction, maxDistance);

		if (debugEnabled && debugManager != null) {
			visualizeRaycast(origin, direction, maxDistance, hit);
		}

		return hit;
	}

	/**
	 * Casts a ray against colliders in a specific layer and visualizes it.
	 */
	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		RaycastHit hit = delegate.raycast(origin, direction, maxDistance, layer);

		if (debugEnabled && debugManager != null) {
			visualizeRaycastWithLayer(origin, direction, maxDistance, hit, layer);
		}

		return hit;
	}

	/**
	 * Casts a ray against colliders in a list of layers and visualizes it.
	 */
	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		RaycastHit hit = delegate.raycast(origin, direction, maxDistance, layers);

		if (debugEnabled && debugManager != null) {
			visualizeRaycastWithLayers(origin, direction, maxDistance, hit, layers);
		}

		return hit;
	}

	/**
	 * Casts a ray and returns all hits along the ray path, sorted by distance.
	 */
	@Override
	public List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance) {
		List<RaycastHit> hits = delegate.raycastAll(origin, direction, maxDistance);

		if (debugEnabled && debugManager != null) {
			if (!hits.isEmpty()) {
				// Visualize all hits
				for (RaycastHit hit : hits) {
					visualizeRaycast(origin, direction, hit.getDistance(), hit);
				}
			} else {
				// No hits, visualize the full ray
				visualizeRaycast(origin, direction, maxDistance, RaycastHit.noHit());
			}
		}

		return hits;
	}

	/**
	 * Casts a ray against all colliders in specific layers and returns all hits.
	 */
	@Override
	public List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		List<RaycastHit> hits = delegate.raycastAll(origin, direction, maxDistance, layers);

		if (debugEnabled && debugManager != null) {
			if (!hits.isEmpty()) {
				// Visualize all hits
				for (RaycastHit hit : hits) {
					visualizeRaycastWithLayers(origin, direction, hit.getDistance(), hit, layers);
				}
			} else {
				// No hits, visualize the full ray
				visualizeRaycastWithLayers(origin, direction, maxDistance, RaycastHit.noHit(), layers);
			}
		}

		return hits;
	}

	/**
	 * Non-allocating variant of raycast. Fills the provided hitInfo object.
	 */
	@Override
	public boolean raycast(Vector2D origin, Vector2D direction, float maxDistance, RaycastHit hitInfo) {
		boolean hit = delegate.raycast(origin, direction, maxDistance, hitInfo);

		if (debugEnabled && debugManager != null) {
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
	 * Validates if a proposed position is valid for entity movement.
	 */
	@Override
	public boolean isPositionValid(Entity entity, Vector2D proposedPosition, CollisionOptions options) {
		boolean isValid = delegate.isPositionValid(entity, proposedPosition, options);

		if (debugEnabled && debugManager != null) {
			Color posColor = isValid ?
				Color.GREEN.deriveColor(0, 1, 1, 0.6) :
				Color.RED.deriveColor(0, 1, 1, 0.6);
			debugManager.drawCircle(proposedPosition, 5, posColor, 0.05f);
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
	 * Gets all entities that overlap a circle with debug visualization.
	 */
	@Override
	public List<Entity> overlapCircle(Vector2D center, float radius) {
		List<Entity> results = delegate.overlapCircle(center, radius);

		if (debugEnabled && debugManager != null) {
			// Use different alpha based on whether anything was found
			Color circleColor = results.isEmpty() ?
				Color.GRAY.deriveColor(0, 1, 1, 0.2) :
				Color.ORANGE.deriveColor(0, 1, 1, 0.4);

			debugManager.drawCircle(center, radius, circleColor, 0.1f);
		}

		return results;
	}

	/**
	 * Gets all entities that overlap a circle in a specific layer.
	 */
	@Override
	public List<Entity> overlapCircle(Vector2D center, float radius, PhysicsLayer layer) {
		List<Entity> results = delegate.overlapCircle(center, radius, layer);

		if (debugEnabled && debugManager != null) {
			// Use different alpha based on whether anything was found
			Color circleColor = results.isEmpty() ?
				Color.GRAY.deriveColor(0, 1, 1, 0.2) :
				Color.ORANGE.deriveColor(0, 1, 1, 0.4);

			debugManager.drawCircle(center, radius, circleColor, 0.1f);
		}

		return results;
	}

	/**
	 * Gets all entities that overlap a box.
	 */
	@Override
	public List<Entity> overlapBox(Vector2D center, float width, float height) {
		return delegate.overlapBox(center, width, height);
	}

	/**
	 * Gets all entities that overlap a box in a specific layer with debug visualization.
	 */
	@Override
	public List<Entity> overlapBox(Vector2D center, float width, float height, PhysicsLayer layer) {
		List<Entity> results = delegate.overlapBox(center, width, height, layer);

		if (debugEnabled && debugManager != null) {
			// Visualize box using four lines
			float halfWidth = width / 2;
			float halfHeight = height / 2;

			// Calculate corners
			Vector2D topLeft = new Vector2D(center.x() - halfWidth, center.y() - halfHeight);
			Vector2D topRight = new Vector2D(center.x() + halfWidth, center.y() - halfHeight);
			Vector2D bottomLeft = new Vector2D(center.x() - halfWidth, center.y() + halfHeight);
			Vector2D bottomRight = new Vector2D(center.x() + halfWidth, center.y() + halfHeight);

			// Use different color based on whether anything was found
			Color boxColor = results.isEmpty() ?
				Color.GRAY.deriveColor(0, 1, 1, 0.2) :
				Color.ORANGE.deriveColor(0, 1, 1, 0.4);

			// Draw the four sides of the box
			float duration = 0.1f;
			debugManager.drawLine(topLeft, topRight, boxColor, duration);
			debugManager.drawLine(topRight, bottomRight, boxColor, duration);
			debugManager.drawLine(bottomRight, bottomLeft, boxColor, duration);
			debugManager.drawLine(bottomLeft, topLeft, boxColor, duration);
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

	// Helper visualization methods

	/**
	 * Visualizes a standard raycast.
	 */
	private void visualizeRaycast(Vector2D origin, Vector2D direction, float maxDistance, RaycastHit hit) {
		// Normalize direction to ensure consistent visualization
		Vector2D normalizedDir = direction.normalize();

		// Calculate actual ray length based on hit results
		float rayLength = hit.isHit() ? hit.getDistance() : maxDistance;
		Vector2D scaledDir = normalizedDir.scale(rayLength);

		// Choose color based on hit result
		Color rayColor = hit.isHit() ? Color.GREEN : Color.RED;

		// Draw the ray
		debugManager.drawRay(origin, scaledDir, rayColor, 0.05f);

		// If hit, draw the normal vector at hit point
		if (hit.isHit() && hit.getNormal() != null && hit.getPoint() != null) {
			debugManager.drawRay(hit.getPoint(), hit.getNormal().scale(10), Color.CYAN, 0.05f);

			// Draw a small circle at hit point for better visibility
			debugManager.drawCircle(hit.getPoint(), 3, Color.YELLOW, 0.05f);
		}
	}

	/**
	 * Visualizes a raycast with layer filtering.
	 */
	private void visualizeRaycastWithLayer(Vector2D origin, Vector2D direction, float maxDistance,
										   RaycastHit hit, PhysicsLayer layer) {
		// Normalize direction
		Vector2D normalizedDir = direction.normalize();

		// Calculate ray length
		float rayLength = hit.isHit() ? hit.getDistance() : maxDistance;
		Vector2D scaledDir = normalizedDir.scale(rayLength);

		// Set color based on layer and hit result
		Color rayColor;
		if (hit.isHit()) {
			rayColor = switch (layer) {
				case OBSTACLE -> Color.RED;
				case PLAYER -> Color.GREEN;
				case ENEMY -> Color.ORANGE;
				default -> Color.YELLOW;
			};
		} else {
			rayColor = Color.DARKGRAY;
		}

		// Draw the ray
		debugManager.drawRay(origin, scaledDir, rayColor, 0.05f);

		// If hit, draw hit point and normal
		if (hit.isHit() && hit.getPoint() != null) {
			debugManager.drawCircle(hit.getPoint(), 3, rayColor.brighter(), 0.05f);

			if (hit.getNormal() != null) {
				debugManager.drawRay(hit.getPoint(), hit.getNormal().scale(10), Color.CYAN, 0.05f);
			}
		}
	}

	/**
	 * Visualizes a raycast with multiple layer filtering.
	 */
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
		} else {
			// Fade color for missed rays
			rayColor = Color.DARKGRAY;
		}

		// Draw the ray
		debugManager.drawRay(origin, scaledDir, rayColor, 0.05f);

		if (hit.isHit() && hit.getPoint() != null) {
			// Draw a small circle at hit point
			debugManager.drawCircle(hit.getPoint(), 3, rayColor.brighter(), 0.05f);

			// Draw normal if available
			if (hit.getNormal() != null) {
				debugManager.drawRay(hit.getPoint(), hit.getNormal().scale(10), Color.CYAN, 0.05f);
			}
		}
	}
}