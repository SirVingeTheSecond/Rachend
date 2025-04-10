package dk.sdu.sem.collisionsystem.raycasting;

import dk.sdu.sem.collision.RaycastResult;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collisionsystem.ColliderNode;
import dk.sdu.sem.collisionsystem.TilemapColliderNode;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.RectangleShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles raycasting.
 */
public class RaycastHandler {
	private static final boolean DEBUG = false;

	/**
	 * Represents a ray for raycasting.
	 */
	public static class Ray {
		private final Vector2D origin;
		private final Vector2D direction;

		public Ray(Vector2D origin, Vector2D direction) {
			this.origin = origin;
			// Normalize direction to ensure consistent behavior
			float mag = direction.magnitude();
			if (mag > 0.0001f) {
				this.direction = direction.scale(1f / mag);
			} else {
				this.direction = new Vector2D(1, 0); // Default to right if zero
			}
		}

		public Vector2D getOrigin() { return origin; }
		public Vector2D getDirection() { return direction; }
		public Vector2D getPoint(float distance) {
			return origin.add(direction.scale(distance));
		}
	}

	/**
	 * Casts a single ray and returns information about what it hit.
	 */
	public RaycastResult raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		if (DEBUG) System.out.println("Casting ray from " + origin + " in direction " + direction);
		Ray ray = new Ray(origin, direction);
		return raycast(ray, maxDistance);
	}

	/**
	 * Casts a ray and returns information about what it hit.
	 */
	public RaycastResult raycast(Ray ray, float maxDistance) {
		// First check collision with regular colliders
		RaycastResult entityHit = checkRegularColliders(ray, maxDistance);

		// Then check collision with tilemap colliders
		RaycastResult tilemapHit = checkTilemapColliders(ray, maxDistance);

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
			return RaycastResult.noHit();
		}
	}

	/**
	 * Checks for collisions with regular colliders.
	 */
	private RaycastResult checkRegularColliders(Ray ray, float maxDistance) {
		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		RaycastResult closestHit = RaycastResult.noHit();
		float closestDistance = maxDistance;

		// Check intersection with each collider
		for (ColliderNode node : colliderNodes) {
			// Skip if node is invalid
			if (!isNodeValid(node)) {
				continue;
			}

			// Skip triggers for ray collision if specified by options
			if (node.collider.isTrigger()) {
				continue;
			}

			// Test ray intersection with collider
			RaycastResult result = testRayCollider(ray, node, maxDistance);

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
	private RaycastResult checkTilemapColliders(Ray ray, float maxDistance) {
		// Get all tilemap collider nodes
		Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

		RaycastResult closestHit = RaycastResult.noHit();
		float closestDistance = maxDistance;

		for (TilemapColliderNode node : tilemapNodes) {
			// Skip if node is invalid
			if (!isTilemapNodeValid(node)) {
				continue;
			}

			// Test ray intersection with tilemap
			RaycastResult result = testRayTilemap(ray, node, maxDistance);

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
	private RaycastResult testRayCollider(Ray ray, ColliderNode node, float maxDistance) {
		// Get entity and collider information
		Vector2D colliderPos = node.transform.getPosition().add(node.collider.getOffset());

		// Different handling based on shape type
		if (node.collider.getCollisionShape() instanceof CircleShape) {
			CircleShape circle = (CircleShape) node.collider.getCollisionShape();
			return testRayCircle(ray, colliderPos, circle.getRadius(), maxDistance, node);
		}
		else if (node.collider.getCollisionShape() instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) node.collider.getCollisionShape();
			return testRayRectangle(ray, colliderPos, rect.getWidth(), rect.getHeight(), maxDistance, node);
		}

		// Unknown shape type - no hit
		return RaycastResult.noHit();
	}

	/**
	 * Tests if a ray intersects with a tilemap.
	 */
	private RaycastResult testRayTilemap(Ray ray, TilemapColliderNode node, float maxDistance) {
		// Get tilemap properties
		Vector2D tilemapPos = node.transform.getPosition();
		int tileSize = node.tilemap.getTileSize();
		int[][] collisionFlags = node.tilemapCollider.getCollisionFlags();

		if (collisionFlags == null || collisionFlags.length == 0) {
			return RaycastResult.noHit();
		}

		// Use DDA (Digital Differential Analysis) algorithm for efficient ray traversal

		// Ray origin relative to tilemap
		Vector2D relativeOrigin = ray.getOrigin().subtract(tilemapPos);

		// Current tile indices
		int tileX = (int)(relativeOrigin.x() / tileSize);
		int tileY = (int)(relativeOrigin.y() / tileSize);

		// Direction of ray
		Vector2D dir = ray.getDirection();

		// Calculate delta distance between tile boundaries
		float deltaDistX = Math.abs(dir.x()) < 0.0001f ? Float.MAX_VALUE : Math.abs(1.0f / dir.x());
		float deltaDistY = Math.abs(dir.y()) < 0.0001f ? Float.MAX_VALUE : Math.abs(1.0f / dir.y());

		// Calculate step direction and initial side distance
		int stepX, stepY;
		float sideDistX, sideDistY;

		if (dir.x() < 0) {
			stepX = -1;
			sideDistX = (relativeOrigin.x() - tileX * tileSize) / tileSize * deltaDistX;
		} else {
			stepX = 1;
			sideDistX = ((tileX + 1) * tileSize - relativeOrigin.x()) / tileSize * deltaDistX;
		}

		if (dir.y() < 0) {
			stepY = -1;
			sideDistY = (relativeOrigin.y() - tileY * tileSize) / tileSize * deltaDistY;
		} else {
			stepY = 1;
			sideDistY = ((tileY + 1) * tileSize - relativeOrigin.y()) / tileSize * deltaDistY;
		}

		// Perform DDA
		boolean hit = false;
		boolean hitX = false; // Was a x-side of a wall hit?
		float distance = 0.0f;

		int maxSteps = 100; // Limit iterations to prevent infinite loops
		for (int i = 0; i < maxSteps && !hit && distance < maxDistance; i++) {
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
				tileY >= 0 && tileY < collisionFlags[0].length) {
				if (collisionFlags[tileX][tileY] == 1) { // 1 = solid
					hit = true;
				}
			}
		}

		if (hit) {
			// Calculate exact hit point
			float rayLength = distance * tileSize;
			Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(rayLength));

			// Calculate hit normal
			Vector2D hitNormal;
			if (hitX) {
				hitNormal = new Vector2D(-stepX, 0);
			} else {
				hitNormal = new Vector2D(0, -stepY);
			}

			return new RaycastResult(true, node.getEntity(), hitPoint, hitNormal, rayLength);
		}

		return RaycastResult.noHit();
	}

	/**
	 * Tests if a ray intersects with a circle.
	 */
	private RaycastResult testRayCircle(
		Ray ray, Vector2D circleCenter, float radius,
		float maxDistance, ColliderNode node) {

		// Vector from ray origin to circle center
		Vector2D toCenter = circleCenter.subtract(ray.getOrigin());

		// Project this vector onto the ray direction
		float projection = toCenter.x() * ray.getDirection().x() +
			toCenter.y() * ray.getDirection().y();

		// If negative, circle is behind ray
		if (projection < 0) {
			return RaycastResult.noHit();
		}

		// If projection is greater than max distance, circle is too far
		if (projection > maxDistance) {
			return RaycastResult.noHit();
		}

		// Find closest point on ray to circle center
		Vector2D closestPoint = ray.getOrigin().add(ray.getDirection().scale(projection));

		// Calculate distance squared from closest point to circle center
		float distanceSquared = closestPoint.subtract(circleCenter).magnitudeSquared();

		// Check if closest point is within circle radius
		if (distanceSquared > radius * radius) {
			return RaycastResult.noHit();
		}

		// Calculate distance from ray origin to circle intersection point
		// Using Pythagoras: d = p - sqrt(r^2 - d^2)
		float distanceToIntersection = projection -
			(float)Math.sqrt(radius * radius - distanceSquared);

		// If intersection is beyond max distance, no hit
		if (distanceToIntersection > maxDistance) {
			return RaycastResult.noHit();
		}

		// Calculate hit point and normal
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(distanceToIntersection));
		Vector2D hitNormal = hitPoint.subtract(circleCenter).normalize();

		return new RaycastResult(true, node.getEntity(), hitPoint, hitNormal, distanceToIntersection);
	}

	/**
	 * Tests if a ray intersects with a rectangle.
	 */
	private RaycastResult testRayRectangle(
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

		// If tmax < 0, ray is going away from object, or
		// if tmin > tmax, ray doesn't intersect
		if (tmax < 0 || tmin > tmax) {
			return RaycastResult.noHit();
		}

		// If tmin is negative, ray starts inside the rectangle
		if (tmin < 0) {
			tmin = 0;
		}

		// Check if hit is within max distance
		if (tmin > maxDistance) {
			return RaycastResult.noHit();
		}

		// Calculate hit point
		Vector2D hitPoint = ray.getOrigin().add(ray.getDirection().scale(tmin));

		// Determine hit normal based on which face was hit
		Vector2D hitNormal;

		// Small epsilon to handle edge cases
		float epsilon = 0.0001f;

		// Check which face the ray hit
		float distToLeft = Math.abs(hitPoint.x() - minX);
		float distToRight = Math.abs(hitPoint.x() - maxX);
		float distToTop = Math.abs(hitPoint.y() - minY);
		float distToBottom = Math.abs(hitPoint.y() - maxY);

		if (distToLeft < epsilon) {
			hitNormal = new Vector2D(-1, 0);
		} else if (distToRight < epsilon) {
			hitNormal = new Vector2D(1, 0);
		} else if (distToTop < epsilon) {
			hitNormal = new Vector2D(0, -1);
		} else if (distToBottom < epsilon) {
			hitNormal = new Vector2D(0, 1);
		} else {
			// If we can't determine exact face, use direction from center
			hitNormal = hitPoint.subtract(rectCenter).normalize();
		}

		return new RaycastResult(true, node.getEntity(), hitPoint, hitNormal, tmin);
	}

	/**
	 * Performs dynamic raycasting around a collider in the direction of movement.
	 */
	public RaycastResult[] castDynamicRays(ColliderNode collider, Vector2D direction, RaycastOptions options) {
		// Skip if invalid
		if (collider == null || direction == null || direction.magnitudeSquared() < 0.0001f) {
			return new RaycastResult[0];
		}

		// Generate rays based on movement direction
		Ray[] rays = generateRays(collider, direction, options);

		// Cast each ray
		RaycastResult[] results = new RaycastResult[rays.length];
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

		// Determine which sides to cast from
		boolean castRight = normDir.x() > 0.1f;
		boolean castLeft = normDir.x() < -0.1f;
		boolean castDown = normDir.y() > 0.1f;
		boolean castUp = normDir.y() < -0.1f;

		// If direction is near-diagonal, cast from both relevant sides
		if (Math.abs(normDir.x()) > 0.4f && Math.abs(normDir.y()) > 0.4f) {
			// Keep both directions active for diagonal movement
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

		// Count active sides
		List<Boolean> activeSides = new ArrayList<>();
		if (castRight) activeSides.add(true);
		if (castLeft) activeSides.add(true);
		if (castUp) activeSides.add(true);
		if (castDown) activeSides.add(true);

		// Default to at least one side if no direction
		if (activeSides.isEmpty()) {
			castRight = true;
			activeSides.add(true);
		}

		// Allocate rays array
		int raysPerSide = options.getRaysPerSide();
		Ray[] rays = new Ray[activeSides.size() * raysPerSide];

		// Calculate bounds for ray positions
		float bounds = 0;
		if (collider.collider.getCollisionShape() instanceof CircleShape) {
			CircleShape circle = (CircleShape) collider.collider.getCollisionShape();
			bounds = circle.getRadius();
		}
		else if (collider.collider.getCollisionShape() instanceof RectangleShape) {
			RectangleShape rect = (RectangleShape) collider.collider.getCollisionShape();
			bounds = Math.max(rect.getWidth(), rect.getHeight()) / 2;
		} else {
			bounds = 10; // Default bounds if shape is unknown
		}

		int rayIndex = 0;

		// Generate rays from right side
		if (castRight) {
			for (int i = 0; i < raysPerSide; i++) {
				float t = (i + 0.5f) / raysPerSide; // Distribute evenly
				float y = worldPos.y() - bounds + (2 * bounds * t);

				Vector2D origin = new Vector2D(worldPos.x() + bounds, y);
				rays[rayIndex++] = new Ray(origin, new Vector2D(1, 0));
			}
		}

		// Generate rays from left side
		if (castLeft) {
			for (int i = 0; i < raysPerSide; i++) {
				float t = (i + 0.5f) / raysPerSide; // Distribute evenly
				float y = worldPos.y() - bounds + (2 * bounds * t);

				Vector2D origin = new Vector2D(worldPos.x() - bounds, y);
				rays[rayIndex++] = new Ray(origin, new Vector2D(-1, 0));
			}
		}

		// Generate rays from top side
		if (castUp) {
			for (int i = 0; i < raysPerSide; i++) {
				float t = (i + 0.5f) / raysPerSide; // Distribute evenly
				float x = worldPos.x() - bounds + (2 * bounds * t);

				Vector2D origin = new Vector2D(x, worldPos.y() - bounds);
				rays[rayIndex++] = new Ray(origin, new Vector2D(0, -1));
			}
		}

		// Generate rays from bottom side
		if (castDown) {
			for (int i = 0; i < raysPerSide; i++) {
				float t = (i + 0.5f) / raysPerSide; // Distribute evenly
				float x = worldPos.x() - bounds + (2 * bounds * t);

				Vector2D origin = new Vector2D(x, worldPos.y() + bounds);
				rays[rayIndex++] = new Ray(origin, new Vector2D(0, 1));
			}
		}

		return rays;
	}

	/**
	 * Checks if a node is valid for raycasting.
	 */
	private boolean isNodeValid(ColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.collider != null &&
			node.collider.getCollisionShape() != null;
	}

	/**
	 * Checks if a tilemap node is valid for raycasting.
	 */
	private boolean isTilemapNodeValid(TilemapColliderNode node) {
		return node != null &&
			node.getEntity() != null &&
			node.getEntity().getScene() != null &&
			node.transform != null &&
			node.tilemap != null &&
			node.tilemapCollider != null &&
			node.tilemapCollider.getCollisionFlags() != null;
	}
}