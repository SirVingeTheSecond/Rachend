package dk.sdu.sem.collisionsystem.raycasting;

import dk.sdu.sem.collision.RaycastHit;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.TilemapColliderNode;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.collision.shapes.CircleShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles raycasting against scene objects.
 * Provides methods to cast rays against colliders and tilemaps.
 */
public class RaycastHandler {
	private static final float MIN_DELTA = 0.0001f;
	private static final int MAX_TILEMAP_STEPS = 100;

	/**
	 * Represents a ray for raycasting.
	 */
	public static class Ray {
		private final Vector2D origin;
		private final Vector2D direction;

		/**
		 * Creates a ray with a normalized direction vector.
		 *
		 * @param origin The starting point of the ray
		 * @param direction The direction vector (will be normalized)
		 */
		public Ray(Vector2D origin, Vector2D direction) {
			this.origin = origin;

			// Normalize direction to ensure consistent behavior
			float mag = direction.magnitude();
			if (mag > MIN_DELTA) {
				this.direction = direction.scale(1f / mag);
			} else {
				this.direction = new Vector2D(1, 0); // Default to right if zero
			}
		}

		public Vector2D getOrigin() {
			return origin;
		}

		public Vector2D getDirection() {
			return direction;
		}

		/**
		 * Gets a point along the ray at the specified distance.
		 *
		 * @param distance Distance from origin
		 * @return The point at the given distance
		 */
		public Vector2D getPoint(float distance) {
			return origin.add(direction.scale(distance));
		}
	}

	/**
	 * Enum representing the sides of a collider for ray casting.
	 */
	private enum CastingSide {
		RIGHT,
		LEFT,
		UP,
		DOWN
	}

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
		return raycast(ray, maxDistance);
	}

	/**
	 * Casts a ray and returns information about what it hit.
	 *
	 * @param ray The ray to cast
	 * @param maxDistance The maximum distance to check
	 * @return Information about what was hit, or null if nothing was hit
	 */
	public RaycastHit raycast(Ray ray, float maxDistance) {
		// First check collision with regular colliders
		RaycastHit entityHit = checkRegularColliders(ray, maxDistance);

		// Then check collision with tilemap colliders
		RaycastHit tilemapHit = checkTilemapColliders(ray, maxDistance);

		// Return the closest hit
		if (entityHit.isHit() && tilemapHit.isHit()) {
			// Both hit something, return the closest
			return entityHit.getDistance() < tilemapHit.getDistance() ? entityHit : tilemapHit;
		}
		else if (entityHit.isHit()) {
			return entityHit;
		}
		else if (tilemapHit.isHit()) {
			return tilemapHit;
		}
		else {
			return RaycastHit.noHit();
		}
	}

	/**
	 * Checks for collisions with regular colliders.
	 */
	private RaycastHit checkRegularColliders(Ray ray, float maxDistance) {
		// Get valid collider nodes
		Set<ColliderNode> validNodes = NodeManager.active().getNodes(ColliderNode.class).stream()
			.filter(NodeValidator::isColliderNodeValid)
			.filter(node -> !node.collider.isTrigger()) // Skip triggers
			.collect(Collectors.toSet());

		RaycastHit closestHit = RaycastHit.noHit();
		float closestDistance = maxDistance;

		// Check intersection with each collider
		for (ColliderNode node : validNodes) {
			RaycastHit result = testRayCollider(ray, node, maxDistance);

			// Keep the closest hit
			if (result.isHit() && result.getDistance() < closestDistance) {
				closestHit = result;
				closestDistance = result.getDistance();
			}
		}

		return closestHit;
	}

	/**
	 * Checks for collisions with tilemap colliders.
	 */
	private RaycastHit checkTilemapColliders(Ray ray, float maxDistance) {
		// Get valid tilemap nodes
		Set<TilemapColliderNode> validNodes = NodeManager.active().getNodes(TilemapColliderNode.class).stream()
			.filter(NodeValidator::isTilemapNodeValid)
			.collect(Collectors.toSet());

		RaycastHit closestHit = RaycastHit.noHit();
		float closestDistance = maxDistance;

		// Check intersection with each tilemap
		for (TilemapColliderNode node : validNodes) {
			RaycastHit result = testRayTilemap(ray, node, maxDistance);

			// Keep the closest hit
			if (result.isHit() && result.getDistance() < closestDistance) {
				closestHit = result;
				closestDistance = result.getDistance();
			}
		}

		return closestHit;
	}

	/**
	 * Tests if a ray intersects with a collider.
	 */
	private RaycastHit testRayCollider(Ray ray, ColliderNode node, float maxDistance) {
		// Get collider position and shape
		Vector2D colliderPos = node.transform.getPosition().add(node.collider.getOffset());

		// Different handling based on shape type
		if (node.collider.getShape() instanceof CircleShape circle) {
			return testRayCircle(ray, colliderPos, circle.getRadius(), maxDistance, node);
		}
		else if (node.collider.getShape() instanceof BoxShape box) {
			return testRayRectangle(ray, colliderPos, box.getWidth(), box.getHeight(), maxDistance, node);
		}

		// Unknown shape type - no hit
		return RaycastHit.noHit();
	}

	/**
	 * Tests if a ray intersects with a tilemap.
	 */
	private RaycastHit testRayTilemap(Ray ray, TilemapColliderNode node, float maxDistance) {
		// Get tilemap properties
		Vector2D tilemapPos = node.transform.getPosition();
		int tileSize = node.tilemap.getTileSize();
		int[][] collisionFlags = node.tilemapCollider.getCollisionFlags();

		if (collisionFlags == null || collisionFlags.length == 0) {
			return RaycastHit.noHit();
		}

		// Use DDA (Digital Differential Analysis) algorithm for ray traversal
		return performDDARaycast(ray, node, tilemapPos, tileSize, collisionFlags, maxDistance);
	}

	/**
	 * Performs Digital Differential Analysis raycasting for tilemaps.
	 * This is an efficient algorithm for grid-based raycasting.
	 */
	private RaycastHit performDDARaycast(
		Ray ray,
		TilemapColliderNode node,
		Vector2D tilemapPos,
		int tileSize,
		int[][] collisionFlags,
		float maxDistance) {

		// Ray origin relative to tilemap
		Vector2D relativeOrigin = ray.getOrigin().subtract(tilemapPos);

		// Current tile indices
		int tileX = (int)(relativeOrigin.x() / tileSize);
		int tileY = (int)(relativeOrigin.y() / tileSize);

		// Direction of ray
		Vector2D dir = ray.getDirection();

		// Calculate delta distance (distance to next cell boundary)
		float deltaDistX = Math.abs(dir.x()) < MIN_DELTA ? Float.MAX_VALUE : Math.abs(1.0f / dir.x());
		float deltaDistY = Math.abs(dir.y()) < MIN_DELTA ? Float.MAX_VALUE : Math.abs(1.0f / dir.y());

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
		boolean hitX = false; // Was a x-side of a wall hit?
		float distance = 0.0f;

		// Limit iterations to prevent infinite loops
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

			return new RaycastHit(
				true,                // hit
				node.getEntity(),    // entity
				hitPoint,            // point
				hitNormal,           // normal
				rayLength,           // distance
				node.tilemapCollider // collider
			);
		}

		return RaycastHit.noHit();
	}

	/**
	 * Tests if a ray intersects with a circle.
	 */
	private RaycastHit testRayCircle(
		Ray ray, Vector2D circleCenter, float radius,
		float maxDistance, ColliderNode node) {

		// Vector from ray origin to circle center
		Vector2D toCenter = circleCenter.subtract(ray.getOrigin());

		// Project this vector onto the ray direction
		float projection = toCenter.dot(ray.getDirection());

		// Early exit conditions
		if (projection < 0 || projection > maxDistance) {
			return RaycastHit.noHit();
		}

		// Find closest point on ray to circle center
		Vector2D closestPoint = ray.getOrigin().add(ray.getDirection().scale(projection));
		float distanceSquared = closestPoint.subtract(circleCenter).magnitudeSquared();

		// Check if closest point is within circle radius
		if (distanceSquared > radius * radius) {
			return RaycastHit.noHit();
		}

		// Calculate distance from ray origin to circle intersection point
		// Using Pythagoras: d = p - sqrt(r^2 - d^2)
		float distanceToIntersection = projection -
			(float)Math.sqrt(radius * radius - distanceSquared);

		// If intersection is beyond max distance, no hit
		if (distanceToIntersection > maxDistance) {
			return RaycastHit.noHit();
		}

		// Calculate hit point and normal
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(distanceToIntersection));
		Vector2D hitNormal = hitPoint.subtract(circleCenter).normalize();

		return new RaycastHit(
			true,               // hit
			node.getEntity(),   // entity
			hitPoint,           // point
			hitNormal,          // normal
			distanceToIntersection, // distance
			node.collider       // collider
		);
	}

	/**
	 * Tests if a ray intersects with a rectangle.
	 */
	private RaycastHit testRayRectangle(
		Ray ray, Vector2D rectCenter, float width, float height,
		float maxDistance, ColliderNode node) {

		// Calculate rectangle bounds
		float halfWidth = width / 2f;
		float halfHeight = height / 2f;
		float minX = rectCenter.x() - halfWidth;
		float maxX = rectCenter.x() + halfWidth;
		float minY = rectCenter.y() - halfHeight;
		float maxY = rectCenter.y() + halfHeight;

		// Calculate inverse of ray direction to avoid divisions
		float invDirX = 1f / ray.getDirection().x();
		float invDirY = 1f / ray.getDirection().y();

		// Calculate intersection with each boundary plane
		float tx1 = (minX - ray.getOrigin().x()) * invDirX;
		float tx2 = (maxX - ray.getOrigin().x()) * invDirX;
		float ty1 = (minY - ray.getOrigin().y()) * invDirY;
		float ty2 = (maxY - ray.getOrigin().y()) * invDirY;

		// Find entry and exit points
		float tmin = Math.max(Math.min(tx1, tx2), Math.min(ty1, ty2));
		float tmax = Math.min(Math.max(tx1, tx2), Math.max(ty1, ty2));

		// Early exit conditions
		if (tmax < 0 || tmin > tmax || tmin > maxDistance) {
			return RaycastHit.noHit();
		}

		// If tmin is negative, ray starts inside the rectangle
		if (tmin < 0) {
			tmin = 0;
		}

		// Calculate hit point
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(tmin));

		// Determine hit normal based on which face was hit
		float epsilon = 0.0001f; // Small epsilon to handle edge cases
		Vector2D hitNormal = determineHitNormal(hitPoint, rectCenter, minX, maxX, minY, maxY, epsilon);

		return new RaycastHit(
			true,            // hit
			node.getEntity(), // entity
			hitPoint,        // point
			hitNormal,       // normal
			tmin,            // distance
			node.collider    // collider
		);
	}

	/**
	 * Determines the hit normal for a box collision.
	 */
	private Vector2D determineHitNormal(
		Vector2D hitPoint, Vector2D rectCenter,
		float minX, float maxX, float minY, float maxY,
		float epsilon) {

		// Check which face the ray hit
		float distToLeft = Math.abs(hitPoint.x() - minX);
		float distToRight = Math.abs(hitPoint.x() - maxX);
		float distToTop = Math.abs(hitPoint.y() - minY);
		float distToBottom = Math.abs(hitPoint.y() - maxY);

		// Find the closest face
		if (distToLeft < epsilon) {
			return new Vector2D(-1, 0);
		} else if (distToRight < epsilon) {
			return new Vector2D(1, 0);
		} else if (distToTop < epsilon) {
			return new Vector2D(0, -1);
		} else if (distToBottom < epsilon) {
			return new Vector2D(0, 1);
		} else {
			// If we can't determine exact face, use direction from center
			return hitPoint.subtract(rectCenter).normalize();
		}
	}

	/**
	 * Performs dynamic raycasting around a collider in the direction of movement.
	 * Useful for predictive collision detection.
	 *
	 * @param collider The collider to cast rays from
	 * @param direction The direction of movement
	 * @param options Raycasting options
	 * @return Array of raycast hits
	 */
	public RaycastHit[] castDynamicRays(ColliderNode collider, Vector2D direction, RaycastOptions options) {
		// Skip if parameters are invalid
		if (collider == null || direction == null || direction.magnitudeSquared() < MIN_DELTA) {
			return new RaycastHit[0];
		}

		// Generate rays based on movement direction
		Ray[] rays = generateRays(collider, direction, options);

		// Cast each ray
		RaycastHit[] results = new RaycastHit[rays.length];
		for (int i = 0; i < rays.length; i++) {
			results[i] = raycast(rays[i], options.getRayLength());
		}

		return results;
	}

	/**
	 * Generates rays around a collider based on movement direction.
	 */
	private Ray[] generateRays(ColliderNode collider, Vector2D direction, RaycastOptions options) {
		// Get collider position and shape information
		Vector2D position = collider.transform.getPosition();
		Vector2D offset = collider.collider.getOffset();
		Vector2D worldPos = position.add(offset);

		// Get normalized direction
		Vector2D normDir = direction.normalize();

		// Determine active casting sides based on direction
		List<CastingSide> activeSides = determineActiveSides(normDir);

		// Default to at least one side if no direction
		if (activeSides.isEmpty()) {
			activeSides.add(CastingSide.RIGHT);
		}

		// Allocate rays array
		int raysPerSide = options.getRaysPerSide();
		Ray[] rays = new Ray[activeSides.size() * raysPerSide];

		// Calculate bounds for ray positions
		float bounds = calculateColliderBounds(collider);

		// Generate rays
		int rayIndex = 0;
		for (CastingSide side : activeSides) {
			for (int i = 0; i < raysPerSide; i++) {
				rays[rayIndex++] = createRayForSide(side, worldPos, bounds, i, raysPerSide);
			}
		}

		return rays;
	}

	/**
	 * Determines which sides to cast rays from based on movement direction.
	 */
	private List<CastingSide> determineActiveSides(Vector2D normDir) {
		List<CastingSide> activeSides = new ArrayList<>();

		boolean castRight = normDir.x() > 0.1f;
		boolean castLeft = normDir.x() < -0.1f;
		boolean castDown = normDir.y() > 0.1f;
		boolean castUp = normDir.y() < -0.1f;

		// For diagonal movement, cast from both relevant sides
		if (Math.abs(normDir.x()) > 0.4f && Math.abs(normDir.y()) > 0.4f) {
			// Keep all active directions for diagonal movement
		} else {
			// For more directional movement, prioritize the dominant direction
			if (Math.abs(normDir.x()) > Math.abs(normDir.y())) {
				castUp = false;
				castDown = false;
			} else {
				castLeft = false;
				castRight = false;
			}
		}

		if (castRight) activeSides.add(CastingSide.RIGHT);
		if (castLeft) activeSides.add(CastingSide.LEFT);
		if (castUp) activeSides.add(CastingSide.UP);
		if (castDown) activeSides.add(CastingSide.DOWN);

		return activeSides;
	}

	/**
	 * Calculates the bounds of a collider for ray generation.
	 */
	private float calculateColliderBounds(ColliderNode collider) {
		if (collider.collider.getShape() instanceof CircleShape circle) {
			return circle.getRadius();
		}
		else if (collider.collider.getShape() instanceof BoxShape box) {
			return Math.max(box.getWidth(), box.getHeight()) / 2;
		}
		return 10; // Default bounds if shape is unknown
	}

	/**
	 * Creates a ray for a specific side of the collider.
	 */
	private Ray createRayForSide(CastingSide side, Vector2D worldPos, float bounds, int rayIndex, int raysPerSide) {
		// Calculate distribution along the side
		float t = (rayIndex + 0.5f) / raysPerSide;

		switch (side) {
			case RIGHT:
				return new Ray(
					new Vector2D(worldPos.x() + bounds, worldPos.y() - bounds + (2 * bounds * t)),
					new Vector2D(1, 0)
				);
			case LEFT:
				return new Ray(
					new Vector2D(worldPos.x() - bounds, worldPos.y() - bounds + (2 * bounds * t)),
					new Vector2D(-1, 0)
				);
			case UP:
				return new Ray(
					new Vector2D(worldPos.x() - bounds + (2 * bounds * t), worldPos.y() - bounds),
					new Vector2D(0, -1)
				);
			case DOWN:
				return new Ray(
					new Vector2D(worldPos.x() - bounds + (2 * bounds * t), worldPos.y() + bounds),
					new Vector2D(0, 1)
				);
			default:
				// Should never happen, but provide a default
				return new Ray(worldPos, new Vector2D(1, 0));
		}
	}
}