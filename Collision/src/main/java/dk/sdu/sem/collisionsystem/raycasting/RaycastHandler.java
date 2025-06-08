package dk.sdu.sem.collisionsystem.raycasting;

import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.Ray;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.broadphase.BroadphaseStrategy;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles raycasting against scene colliders and tilemaps.
 * This class provides methods for casting rays against the scene and determining what they hit.
 */
public class RaycastHandler {
	private static final Logging LOGGER = Logging.createLogger("RaycastHandler", LoggingLevel.DEBUG);
	private static final float EPSILON = 0.0001f;
	private static final int MAX_TILEMAP_STEPS = 100;

	private final BroadphaseStrategy spatialPartitioning;

	/**
	 * Creates a new raycast handler with spatial partitioning optimization.
	 *
	 * @param spatialPartitioning The spatial partitioning to use for optimizing raycasts
	 */
	public RaycastHandler(BroadphaseStrategy spatialPartitioning) {
		this.spatialPartitioning = spatialPartitioning;
	}

	/**
	 * Creates a new raycast handler without spatial partitioning optimization.
	 */
	public RaycastHandler() {
		this.spatialPartitioning = null;
	}

	/**
	 * Casts a ray and returns information about the first hit.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @return Information about what was hit, or a no-hit result
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		validateRaycastParams(origin, direction, maxDistance);

		List<RaycastHit> allHits = raycastInternal(origin, direction, maxDistance, null, 1);
		return allHits.isEmpty() ? RaycastHit.noHit() : allHits.get(0);
	}

	/**
	 * Casts a ray against colliders in a specific physics layer.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param layer The physics layer to filter by
	 * @return Information about what was hit, or a no-hit result if nothing was hit
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		validateRaycastParams(origin, direction, maxDistance);

		List<RaycastHit> allHits = raycastInternal(origin, direction, maxDistance,
			layer != null ? List.of(layer) : null, 1);

		return allHits.isEmpty() ? RaycastHit.noHit() : allHits.get(0);
	}

	/**
	 * Casts a ray against colliders in a list of layers.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param layers The physics layers to filter by
	 * @return Information about what was hit, or a no-hit result if nothing was hit
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		validateRaycastParams(origin, direction, maxDistance);

		List<RaycastHit> allHits = raycastInternal(origin, direction, maxDistance, layers, 1);
		return allHits.isEmpty() ? RaycastHit.noHit() : allHits.get(0);
	}

	/**
	 * Casts a ray and returns all hits along the ray, sorted by distance.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @return List of all hits along the ray, sorted by distance (empty if no hits)
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance) {
		validateRaycastParams(origin, direction, maxDistance);

		return raycastInternal(origin, direction, maxDistance, null, Integer.MAX_VALUE);
	}

	/**
	 * Casts a ray and returns all hits along the ray in specific layers, sorted by distance.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param layers The physics layers to filter by
	 * @return List of all hits along the ray, sorted by distance (empty if no hits)
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public List<RaycastHit> raycastAll(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		validateRaycastParams(origin, direction, maxDistance);

		return raycastInternal(origin, direction, maxDistance, layers, Integer.MAX_VALUE);
	}

	/**
	 * Non-allocating version of raycast. Fills the provided hitInfo object.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param hitInfo The RaycastHit object to fill with hit information
	 * @return true if something was hit, false otherwise
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public boolean raycast(Vector2D origin, Vector2D direction, float maxDistance, RaycastHit hitInfo) {
		validateRaycastParams(origin, direction, maxDistance);
		if (hitInfo == null) {
			throw new IllegalArgumentException("hitInfo cannot be null");
		}

		List<RaycastHit> hits = raycastInternal(origin, direction, maxDistance, null, 1);
		if (hits.isEmpty()) {
			// No hit - reset the hitInfo object to a no-hit state
			hitInfo.setHit(false);
			hitInfo.setEntity(null);
			hitInfo.setPoint(null);
			hitInfo.setNormal(null);
			hitInfo.setDistance(0);
			hitInfo.setCollider(null);
			return false;
		}

		// Copy hit information to provided hitInfo object
		RaycastHit hit = hits.get(0);
		hitInfo.setHit(hit.isHit());
		hitInfo.setEntity(hit.getEntity());
		hitInfo.setPoint(hit.getPoint());
		hitInfo.setNormal(hit.getNormal());
		hitInfo.setDistance(hit.getDistance());
		hitInfo.setCollider(hit.getCollider());
		return true;
	}

	/**
	 * Internal implementation of raycasting that supports both single and multiple hit scenarios.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param layers The physics layers to filter by (null for all layers)
	 * @param maxHits Maximum number of hits to collect (1 for first hit only, Integer.MAX_VALUE for all hits)
	 * @return List of hits along the ray, sorted by distance (empty if no hits)
	 */
	private List<RaycastHit> raycastInternal(Vector2D origin, Vector2D direction, float maxDistance,
											 List<PhysicsLayer> layers, int maxHits) {
		Ray ray = new Ray(origin, direction);
		List<RaycastHit> hits = new ArrayList<>();

		// spatial partitioning to get only relevant colliders
		Set<ColliderNode> potentialColliders;
		if (spatialPartitioning != null) {
			potentialColliders = spatialPartitioning.getPotentialCollidersAlongRay(origin, direction, maxDistance);
		} else {
			// if spatial partitioning is not available
			potentialColliders = NodeManager.active().getNodes(ColliderNode.class);
		}

		// Filter by layer if needed
		Set<ColliderNode> filteredColliders = potentialColliders;
		if (layers != null && !layers.isEmpty()) {
			filteredColliders = potentialColliders.stream()
				.filter(NodeValidator::isColliderNodeValid)
				.filter(node -> layers.contains(node.collider.getLayer()))
				.collect(Collectors.toSet());
		}

		// Get tilemaps with the specified layer
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class).stream()
			.filter(NodeValidator::isTilemapNodeValid)
			.filter(node -> layers == null || layers.contains(node.collider.getLayer()))
			.collect(Collectors.toSet());

		// Collect collider hits
		List<RaycastHit> colliderHits = castRayAgainstCollidersAll(ray, filteredColliders, maxDistance);

		// Collect tilemap hits
		List<RaycastHit> tilemapHits = castRayAgainstTilemapsAll(ray, tilemapNodes, maxDistance);

		// Combine hits and sort by distance
		hits.addAll(colliderHits);
		hits.addAll(tilemapHits);
		hits.sort(Comparator.comparing(RaycastHit::getDistance));

		// Return all hits or limit to maxHits
		return hits.isEmpty() ? Collections.emptyList()
			: hits.subList(0, Math.min(maxHits, hits.size()));
	}

	/**
	 * Casts a ray against all colliders and returns all hits.
	 *
	 * @param ray The ray to cast
	 * @param colliders The set of colliders to check
	 * @param maxDistance The maximum distance to check
	 * @return List of hits sorted by distance
	 */
	private List<RaycastHit> castRayAgainstCollidersAll(Ray ray, Set<ColliderNode> colliders, float maxDistance) {
		List<RaycastHit> hits = new ArrayList<>();

		for (ColliderNode node : colliders) {
			if (!NodeValidator.isColliderNodeValid(node)) {
				continue;
			}

			RaycastHit hit = castRayAgainstCollider(ray, node, maxDistance);
			if (hit.isHit() && hit.getDistance() <= maxDistance) {
				hits.add(hit);
			}
		}

		return hits;
	}

	/**
	 * Casts a ray against a single collider and returns hit information.
	 *
	 * @param ray The ray to cast
	 * @param node The collider node to check
	 * @param maxDistance The maximum distance to check
	 * @return Information about the hit, or a no-hit result
	 */
	private RaycastHit castRayAgainstCollider(Ray ray, ColliderNode node, float maxDistance) {
		// Get collider info
		ICollisionShape shape = node.collider.getShape();
		Vector2D position = node.transform.getPosition().add(node.collider.getOffset());

		// Cast ray against this collider based on shape type
		if (shape instanceof CircleShape circle) {
			RayCircleResult result = testRayCircle(ray, position, circle.getRadius());

			if (result.hit && result.distance <= maxDistance) {
				return new RaycastHit(
					true,               // hit
					node.getEntity(),   // entity
					result.hitPoint,    // point
					result.hitNormal,   // normal
					result.distance,    // distance
					node.collider       // collider
				);
			}
		}
		else if (shape instanceof BoxShape box) {
			RayBoxResult result = testRayBox(ray, position, box.getWidth(), box.getHeight());

			if (result.hit && result.distance <= maxDistance) {
				return new RaycastHit(
					true,               // hit
					node.getEntity(),   // entity
					result.hitPoint,    // point
					result.hitNormal,   // normal
					result.distance,    // distance
					node.collider       // collider
				);
			}
		}

		// No hit or hit too far
		return RaycastHit.noHit();
	}

	/**
	 * Casts a ray against all tilemaps and returns all hits.
	 *
	 * @param ray The ray to cast
	 * @param tilemaps The set of tilemap nodes to check
	 * @param maxDistance The maximum distance to check
	 * @return List of all tilemap hits sorted by distance
	 */
	private List<RaycastHit> castRayAgainstTilemapsAll(Ray ray, Set<TilemapColliderNode> tilemaps, float maxDistance) {
		List<RaycastHit> hits = new ArrayList<>();

		for (TilemapColliderNode node : tilemaps) {
			if (!NodeValidator.isTilemapNodeValid(node)) {
				continue;
			}

			List<RaycastHit> tilemapHits = castRayAgainstTilemapAll(ray, node, maxDistance);
			hits.addAll(tilemapHits);
		}

		return hits;
	}

	/**
	 * Casts a ray against a tilemap and returns all hits.
	 *
	 * @param ray The ray to cast
	 * @param node The tilemap node to check
	 * @param maxDistance The maximum distance to check
	 * @return List of all hits against this tilemap
	 */
	private List<RaycastHit> castRayAgainstTilemapAll(Ray ray, TilemapColliderNode node, float maxDistance) {
		List<RaycastHit> hits = new ArrayList<>();

		// Get tilemap info
		Vector2D tilemapPos = node.transform.getPosition();
		int tileSize = node.tilemap.getTileSize();

		// Get collision flags through the shape
		GridShape gridShape = node.getGridShape();
		int[][] collisionFlags = gridShape.getCollisionFlags();

		// Cast ray against this tilemap using DDA algorithm
		List<RayTilemapResult> results = testRayTilemapAll(ray, tilemapPos, tileSize, collisionFlags, maxDistance);

		// Convert results to RaycastHit objects
		for (RayTilemapResult result : results) {
			if (result.hit && result.distance <= maxDistance) {
				hits.add(new RaycastHit(
					true,                // hit
					node.getEntity(),    // entity
					result.hitPoint,     // point
					result.hitNormal,    // normal
					result.distance,     // distance
					node.collider        // collider
				));
			}
		}

		return hits;
	}

	/**
	 * Validates common raycast parameters.
	 *
	 * @param origin The ray origin
	 * @param direction The ray direction
	 * @param maxDistance The maximum ray distance
	 * @throws IllegalArgumentException if any parameter is invalid
	 */
	private void validateRaycastParams(Vector2D origin, Vector2D direction, float maxDistance) {
		if (origin == null) {
			throw new IllegalArgumentException("Ray origin cannot be null");
		}
		if (direction == null) {
			throw new IllegalArgumentException("Ray direction cannot be null");
		}
		if (Math.abs(direction.x()) < EPSILON && Math.abs(direction.y()) < EPSILON) {
			throw new IllegalArgumentException("Ray direction cannot be zero");
		}
		if (maxDistance <= 0) {
			throw new IllegalArgumentException("Max distance must be positive");
		}
	}

	//-------------------------------------------------------------------------------
	// Ray intersection tests
	//-------------------------------------------------------------------------------

	/**
	 * Tests if a ray intersects with a circle.
	 *
	 * @param ray The ray to test
	 * @param circleCenter The center of the circle
	 * @param radius The radius of the circle
	 * @return Result of the intersection test
	 */
	private RayCircleResult testRayCircle(Ray ray, Vector2D circleCenter, float radius) {
		RayCircleResult result = new RayCircleResult();

		// Special case: ray starting inside the circle
		Vector2D originToCenter = circleCenter.subtract(ray.getOrigin());
		float distanceSquared = originToCenter.magnitudeSquared();
		if (distanceSquared <= radius * radius) {
			// Ray starts inside, return a hit at the origin with normal pointing away from center
			result.hit = true;
			result.distance = 0;
			result.hitPoint = ray.getOrigin();
			result.hitNormal = originToCenter.magnitudeSquared() > EPSILON * EPSILON ?
				originToCenter.normalize().scale(-1) :
				ray.getDirection().scale(-1);
			return result;
		}

		// Vector from ray origin to circle center
		// Project this vector onto the ray direction
		float projection = originToCenter.dot(ray.getDirection());

		// If the circle is behind the ray origin, no intersection
		if (projection < 0) {
			return result;
		}

		// Find the closest point on the ray to the circle center
		Vector2D closestPoint = ray.getOrigin().add(ray.getDirection().scale(projection));
		float closestDistanceSquared = closestPoint.subtract(circleCenter).magnitudeSquared();

		// If the closest point is outside the circle, no intersection
		if (closestDistanceSquared > radius * radius) {
			return result;
		}

		// Calculate the distance from closest point to intersection point
		float offset = (float) Math.sqrt(radius * radius - closestDistanceSquared);

		// Calculate the distance from ray origin to intersection point
		float distance = projection - offset;

		// If intersection is behind the ray, use the second intersection point
		if (distance < 0) {
			distance = projection + offset;

			// If still behind, no valid intersection
			if (distance < 0) {
				return result;
			}
		}

		// Calculate hit point and normal
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(distance));
		Vector2D hitNormal = hitPoint.subtract(circleCenter).normalize();

		// Set result fields
		result.hit = true;
		result.distance = distance;
		result.hitPoint = hitPoint;
		result.hitNormal = hitNormal;

		return result;
	}

	/**
	 * Tests if a ray intersects with a box.
	 *
	 * @param ray The ray to test
	 * @param boxPos The position of the box (top-left corner)
	 * @param width The width of the box
	 * @param height The height of the box
	 * @return Result of the intersection test
	 */
	private RayBoxResult testRayBox(Ray ray, Vector2D boxPos, float width, float height) {
		RayBoxResult result = new RayBoxResult();

		// Box bounds
		float minX = boxPos.x();
		float minY = boxPos.y();
		float maxX = boxPos.x() + width;
		float maxY = boxPos.y() + height;

		// Check if ray origin is inside the box
		Vector2D origin = ray.getOrigin();
		boolean rayStartsInside = (origin.x() >= minX && origin.x() <= maxX &&
			origin.y() >= minY && origin.y() <= maxY);

		if (rayStartsInside) {
			// Ray starts inside the box - return a hit at the origin
			// with normal based on closest face
			result.hit = true;
			result.distance = 0;
			result.hitPoint = origin;

			// Find closest face to determine normal
			float distToLeft = origin.x() - minX;
			float distToRight = maxX - origin.x();
			float distToTop = origin.y() - minY;
			float distToBottom = maxY - origin.y();

			float minDist = Math.min(Math.min(distToLeft, distToRight), Math.min(distToTop, distToBottom));

			if (minDist == distToLeft) {
				result.hitNormal = new Vector2D(-1, 0);
			} else if (minDist == distToRight) {
				result.hitNormal = new Vector2D(1, 0);
			} else if (minDist == distToTop) {
				result.hitNormal = new Vector2D(0, -1);
			} else {
				result.hitNormal = new Vector2D(0, 1);
			}

			return result;
		}

		// Ray origin and direction
		Vector2D dir = ray.getDirection();

		// Calculate inverse of direction to avoid divisions
		float invDirX = Math.abs(dir.x()) > EPSILON ? 1f / dir.x() : Float.MAX_VALUE;
		float invDirY = Math.abs(dir.y()) > EPSILON ? 1f / dir.y() : Float.MAX_VALUE;

		// Calculate intersection distances for each box face
		float t1 = (minX - origin.x()) * invDirX;
		float t2 = (maxX - origin.x()) * invDirX;
		float t3 = (minY - origin.y()) * invDirY;
		float t4 = (maxY - origin.y()) * invDirY;

		// Find intersection with near and far faces on each axis
		float tmin = Math.max(Math.min(t1, t2), Math.min(t3, t4));
		float tmax = Math.min(Math.max(t1, t2), Math.max(t3, t4));

		// If tmax < 0, ray is intersecting box but completely in the opposite direction
		// If tmin > tmax, ray doesn't intersect box
		if (tmax < 0 || tmin > tmax) {
			return result;
		}

		// If tmin < 0, ray starts inside the box
		float distance = tmin >= 0 ? tmin : tmax;

		// Calculate hit point
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(distance));

		// Determine which face was hit to calculate normal
		Vector2D hitNormal;
		float epsilon = 0.0001f;

		if (Math.abs(distance - t1) < epsilon && dir.x() < 0) {
			hitNormal = new Vector2D(1, 0); // Left face, normal points right
		} else if (Math.abs(distance - t2) < epsilon && dir.x() > 0) {
			hitNormal = new Vector2D(-1, 0);  // Right face, normal points left
		} else if (Math.abs(distance - t3) < epsilon && dir.y() < 0) {
			hitNormal = new Vector2D(0, 1); // Top face, normal points down
		} else {
			hitNormal = new Vector2D(0, -1);  // Bottom face, normal points up
		}

		// Set result fields
		result.hit = true;
		result.distance = distance;
		result.hitPoint = hitPoint;
		result.hitNormal = hitNormal;

		return result;
	}

	/**
	 * Tests if a ray intersects with a tilemap. Returns all hits.
	 * Uses Digital Differential Analysis (DDA) algorithm.
	 *
	 * @param ray The ray to test
	 * @param tilemapPos The position of the tilemap
	 * @param tileSize The size of each tile
	 * @param collisionFlags The collision flags for the tilemap
	 * @param maxDistance The maximum distance to check
	 * @return List of results for all intersections
	 */
	private List<RayTilemapResult> testRayTilemapAll(
		Ray ray, Vector2D tilemapPos, int tileSize, int[][] collisionFlags, float maxDistance) {

		List<RayTilemapResult> results = new ArrayList<>();

		int gridWidth = collisionFlags.length;
		int gridHeight = collisionFlags[0].length;

		// Ray origin and direction
		Vector2D origin = ray.getOrigin();
		Vector2D dir = ray.getDirection();

		// Ray origin relative to tilemap
		Vector2D relativeOrigin = origin.subtract(tilemapPos);

		// Starting tile coordinates (can be outside the grid)
		float startX = relativeOrigin.x() / tileSize;
		float startY = relativeOrigin.y() / tileSize;

		// Current tile indices
		int tileX = (int) Math.floor(startX);
		int tileY = (int) Math.floor(startY);

		// Calculate delta distance (distance to next cell boundary)
		float deltaDistX = Math.abs(dir.x()) < EPSILON ? Float.MAX_VALUE : Math.abs(1.0f / dir.x());
		float deltaDistY = Math.abs(dir.y()) < EPSILON ? Float.MAX_VALUE : Math.abs(1.0f / dir.y());

		// Calculate step direction and initial side distance
		int stepX = dir.x() < 0 ? -1 : 1;
		int stepY = dir.y() < 0 ? -1 : 1;

		// Calculate distance to first grid intersection
		float sideDistX;
		if (dir.x() < 0) {
			sideDistX = (startX - tileX) * deltaDistX;
		} else {
			sideDistX = (tileX + 1 - startX) * deltaDistX;
		}

		float sideDistY;
		if (dir.y() < 0) {
			sideDistY = (startY - tileY) * deltaDistY;
		} else {
			sideDistY = (tileY + 1 - startY) * deltaDistY;
		}

		// Perform DDA
		float distance = 0;
		float totalDistance = 0;

		for (int i = 0; i < MAX_TILEMAP_STEPS && totalDistance < maxDistance; i++) {
			// Jump to next tile in x or y direction
			boolean hitX;
			if (sideDistX < sideDistY) {
				tileX += stepX;
				distance = sideDistX;
				sideDistX += deltaDistX;
				hitX = true;
			} else {
				tileY += stepY;
				distance = sideDistY;
				sideDistY += deltaDistY;
				hitX = false;
			}

			totalDistance = distance * tileSize;

			// Check if tile is within bounds and solid
			if (tileX >= 0 && tileX < gridWidth && tileY >= 0 && tileY < gridHeight &&
				collisionFlags[tileX][tileY] == 1) {

				// Calculate hit point
				Vector2D hitPoint = origin.add(dir.scale(totalDistance));

				// Calculate hit normal based on which face was hit
				Vector2D hitNormal = hitX ?
					new Vector2D(-stepX, 0) :
					new Vector2D(0, -stepY);

				// Create result
				RayTilemapResult result = new RayTilemapResult();
				result.hit = true;
				result.distance = totalDistance;
				result.hitPoint = hitPoint;
				result.hitNormal = hitNormal;
				result.tileX = tileX;
				result.tileY = tileY;

				results.add(result);

				// For raycastAll we continue, for single raycast we'd return here
			}
		}

		return results;
	}

	//-------------------------------------------------------------------------------
	// Result classes for ray intersection tests
	//-------------------------------------------------------------------------------

	/**
	 * Result of a ray-circle intersection test.
	 */
	private static class RayCircleResult {
		boolean hit = false;
		float distance = Float.MAX_VALUE;
		Vector2D hitPoint = null;
		Vector2D hitNormal = null;
	}

	/**
	 * Result of a ray-box intersection test.
	 */
	private static class RayBoxResult {
		boolean hit = false;
		float distance = Float.MAX_VALUE;
		Vector2D hitPoint = null;
		Vector2D hitNormal = null;
	}

	/**
	 * Result of a ray-tilemap intersection test.
	 */
	private static class RayTilemapResult {
		boolean hit = false;
		float distance = Float.MAX_VALUE;
		Vector2D hitPoint = null;
		Vector2D hitNormal = null;
		int tileX = -1;
		int tileY = -1;
	}
}