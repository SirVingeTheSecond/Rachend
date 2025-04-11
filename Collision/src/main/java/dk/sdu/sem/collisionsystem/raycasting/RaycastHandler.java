package dk.sdu.sem.collisionsystem.raycasting;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.Ray;
import dk.sdu.sem.collision.RaycastHit;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles raycasting against scene colliders and tilemaps.
 */
public class RaycastHandler {
	private static final float EPSILON = 0.0001f;
	private static final int MAX_TILEMAP_STEPS = 100;

	/**
	 * Casts a ray and returns information about what it hit.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @return Information about what was hit, or null if nothing was hit
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		Ray ray = new Ray(origin, direction);

		// Get all collider nodes
		Set<ColliderNode> allColliders = NodeManager.active().getNodes(ColliderNode.class);

		// Get all tilemaps
		Set<TilemapColliderNode> tilemaps = NodeManager.active().getNodes(TilemapColliderNode.class);

		// Cast ray against colliders and tilemaps
		RaycastHit colliderHit = castRayAgainstColliders(ray, allColliders, maxDistance);
		RaycastHit tilemapHit = castRayAgainstTilemaps(ray, tilemaps, maxDistance);

		// Return the closest hit
		if (colliderHit.isHit() && tilemapHit.isHit()) {
			return colliderHit.getDistance() < tilemapHit.getDistance() ? colliderHit : tilemapHit;
		} else if (colliderHit.isHit()) {
			return colliderHit;
		} else if (tilemapHit.isHit()) {
			return tilemapHit;
		}

		return RaycastHit.noHit();
	}

	/**
	 * Casts a ray against colliders in a specific physics layer.
	 *
	 * @param origin The origin of the ray
	 * @param direction The direction of the ray
	 * @param maxDistance The maximum distance to check
	 * @param layer The physics layer to filter by
	 * @return Information about what was hit, or a no-hit result if nothing was hit
	 */
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		Ray ray = new Ray(origin, direction);

		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Filter by physics layer
		List<ColliderNode> filteredColliders = colliderNodes.stream()
			.filter(NodeValidator::isColliderNodeValid)
			.filter(node -> node.collider.getLayer() == layer)
			.toList();

		// Get tilemaps with the specified layer
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class).stream()
			.filter(NodeValidator::isTilemapNodeValid)
			.filter(node -> node.tilemapCollider.getLayer() == layer)
			.collect(Collectors.toSet());

		// Find the closest hit among entities and tilemaps
		RaycastHit colliderHit = castRayAgainstColliders(ray, new HashSet<>(filteredColliders), maxDistance);
		RaycastHit tilemapHit = castRayAgainstTilemaps(ray, tilemapNodes, maxDistance);

		// Return the closest hit
		if (colliderHit.isHit() && tilemapHit.isHit()) {
			return colliderHit.getDistance() < tilemapHit.getDistance() ? colliderHit : tilemapHit;
		} else if (colliderHit.isHit()) {
			return colliderHit;
		} else if (tilemapHit.isHit()) {
			return tilemapHit;
		}

		return RaycastHit.noHit();
	}

	/**
	 * Casts a ray against a set of colliders.
	 */
	private RaycastHit castRayAgainstColliders(Ray ray, Set<ColliderNode> colliders, float maxDistance) {
		RaycastHit closestHit = RaycastHit.noHit();
		float closestDistance = maxDistance;

		for (ColliderNode node : colliders) {
			if (!NodeValidator.isColliderNodeValid(node)) {
				continue;
			}

			// Get collider info
			ICollisionShape shape = node.collider.getShape();
			Vector2D position = node.collider.getWorldPosition();

			// Cast ray against this collider
			float distance = -1;
			Vector2D hitPoint = null;
			Vector2D hitNormal = null;

			if (shape instanceof CircleShape) {
				// Ray vs Circle test
				CircleShape circle = (CircleShape) shape;
				RayCircleResult result = testRayCircle(ray, position, circle.getRadius());

				if (result.hit && result.distance < closestDistance) {
					distance = result.distance;
					hitPoint = result.hitPoint;
					hitNormal = result.hitNormal;
				}
			}
			else if (shape instanceof BoxShape) {
				// Ray vs Box test
				BoxShape box = (BoxShape) shape;
				RayBoxResult result = testRayBox(ray, position, box.getWidth(), box.getHeight());

				if (result.hit && result.distance < closestDistance) {
					distance = result.distance;
					hitPoint = result.hitPoint;
					hitNormal = result.hitNormal;
				}
			}

			// Record the hit if this is the closest so far
			if (distance >= 0 && distance < closestDistance) {
				closestDistance = distance;
				closestHit = new RaycastHit(
					true,              // hit
					node.getEntity(),  // entity
					hitPoint,          // point
					hitNormal,         // normal
					distance,          // distance
					node.collider      // collider
				);
			}
		}

		return closestHit;
	}

	/**
	 * Casts a ray against a set of tilemaps.
	 */
	private RaycastHit castRayAgainstTilemaps(Ray ray, Set<TilemapColliderNode> tilemaps, float maxDistance) {
		RaycastHit closestHit = RaycastHit.noHit();
		float closestDistance = maxDistance;

		for (TilemapColliderNode node : tilemaps) {
			if (!NodeValidator.isTilemapNodeValid(node)) {
				continue;
			}

			// Get tilemap info
			Vector2D tilemapPos = node.transform.getPosition();
			int tileSize = node.tilemap.getTileSize();
			int[][] collisionFlags = node.tilemapCollider.getCollisionFlags();

			// Cast ray against this tilemap
			RayTilemapResult result = testRayTilemap(ray, tilemapPos, tileSize, collisionFlags, maxDistance);

			if (result.hit && result.distance < closestDistance) {
				closestDistance = result.distance;
				closestHit = new RaycastHit(
					true,               // hit
					node.getEntity(),   // entity
					result.hitPoint,    // point
					result.hitNormal,   // normal
					result.distance,    // distance
					node.tilemapCollider // collider
				);
			}
		}

		return closestHit;
	}

	//-------------------------------------------------------------------------------
	// Ray intersection tests
	//-------------------------------------------------------------------------------

	/**
	 * Tests if a ray intersects with a circle.
	 */
	private RayCircleResult testRayCircle(Ray ray, Vector2D circleCenter, float radius) {
		RayCircleResult result = new RayCircleResult();

		// Vector from ray origin to circle center
		Vector2D toCenter = circleCenter.subtract(ray.getOrigin());

		// Project this vector onto the ray direction
		float projection = toCenter.dot(ray.getDirection());

		// If the circle is behind the ray origin, no intersection
		if (projection < 0) {
			return result;
		}

		// Find the closest point on the ray to the circle center
		Vector2D closestPoint = ray.getOrigin().add(ray.getDirection().scale(projection));
		float distanceSquared = closestPoint.subtract(circleCenter).magnitudeSquared();

		// If the closest point is outside the circle, no intersection
		if (distanceSquared > radius * radius) {
			return result;
		}

		// Calculate the distance from closest point to intersection point
		float offset = (float) Math.sqrt(radius * radius - distanceSquared);

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
	 */
	private RayBoxResult testRayBox(Ray ray, Vector2D boxPos, float width, float height) {
		RayBoxResult result = new RayBoxResult();

		// Box bounds
		float minX = boxPos.x();
		float minY = boxPos.y();
		float maxX = boxPos.x() + width;
		float maxY = boxPos.y() + height;

		// Ray origin and direction
		Vector2D origin = ray.getOrigin();
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

		if (Math.abs(distance - t1) < epsilon) {
			hitNormal = new Vector2D(-1, 0); // Left face
		} else if (Math.abs(distance - t2) < epsilon) {
			hitNormal = new Vector2D(1, 0);  // Right face
		} else if (Math.abs(distance - t3) < epsilon) {
			hitNormal = new Vector2D(0, -1); // Top face
		} else {
			hitNormal = new Vector2D(0, 1);  // Bottom face
		}

		// Set result fields
		result.hit = true;
		result.distance = distance;
		result.hitPoint = hitPoint;
		result.hitNormal = hitNormal;

		return result;
	}

	/**
	 * Tests if a ray intersects with a tilemap.
	 */
	private RayTilemapResult testRayTilemap(
		Ray ray, Vector2D tilemapPos, int tileSize, int[][] collisionFlags, float maxDistance) {

		RayTilemapResult result = new RayTilemapResult();

		// Use DDA (Digital Differential Analysis) algorithm for ray traversal

		// Ray origin relative to tilemap
		Vector2D relativeOrigin = ray.getOrigin().subtract(tilemapPos);

		// Current tile indices
		int tileX = (int)(relativeOrigin.x() / tileSize);
		int tileY = (int)(relativeOrigin.y() / tileSize);

		// Direction of ray
		Vector2D dir = ray.getDirection();

		// Calculate delta distance (distance to next cell boundary)
		float deltaDistX = Math.abs(dir.x()) < EPSILON ? Float.MAX_VALUE : Math.abs(1.0f / dir.x());
		float deltaDistY = Math.abs(dir.y()) < EPSILON ? Float.MAX_VALUE : Math.abs(1.0f / dir.y());

		// Calculate step direction and initial side distance
		int stepX = dir.x() < 0 ? -1 : 1;
		int stepY = dir.y() < 0 ? -1 : 1;

		float sideDistX = dir.x() < 0
			? (relativeOrigin.x() - tileX * tileSize) / tileSize * deltaDistX
			: ((tileX + 1) * tileSize - relativeOrigin.x()) / tileSize * deltaDistX;

		float sideDistY = dir.y() < 0
			? (relativeOrigin.y() - tileY * tileSize) / tileSize * deltaDistY
			: ((tileY + 1) * tileSize - relativeOrigin.y()) / tileSize * deltaDistY;

		// Perform DDA
		boolean hit = false;
		boolean hitX = false;
		float distance = 0.0f;

		for (int i = 0; i < MAX_TILEMAP_STEPS && !hit && distance < maxDistance; i++) {
			// Jump to next map square in either x or y direction
			if (sideDistX < sideDistY) {
				sideDistX += deltaDistX;
				tileX += stepX;
				hitX = true;
				distance = sideDistX - deltaDistX;
			} else {
				sideDistY += deltaDistY;
				tileY += stepY;
				hitX = false;
				distance = sideDistY - deltaDistY;
			}

			// Check if ray has hit a wall
			if (tileX >= 0 && tileX < collisionFlags.length &&
				tileY >= 0 && tileY < collisionFlags[0].length &&
				collisionFlags[tileX][tileY] == 1) { // 1 = solid
				hit = true;
			}
		}

		if (hit) {
			// Calculate exact hit point
			float rayLength = distance * tileSize;
			Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(rayLength));

			// Calculate hit normal
			Vector2D hitNormal = hitX
				? new Vector2D(-stepX, 0)
				: new Vector2D(0, -stepY);

			// Set result fields
			result.hit = true;
			result.distance = rayLength;
			result.hitPoint = hitPoint;
			result.hitNormal = hitNormal;
			result.tileX = tileX;
			result.tileY = tileY;
		}

		return result;
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