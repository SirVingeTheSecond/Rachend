package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.collision.data.CollisionOptions;
import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.collision.events.CollisionEnterEvent;
import dk.sdu.sem.collision.events.TriggerEnterEvent;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collisionsystem.events.EventSystem;
import dk.sdu.sem.collisionsystem.narrowphase.NarrowPhaseDetector;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.raycasting.RaycastHandler;
import dk.sdu.sem.collisionsystem.state.CollisionState;
import dk.sdu.sem.collisionsystem.utils.NodeValidator;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the ICollisionSPI interface.
 * Provides methods for querying the collision state.
 */
public class CollisionService implements ICollisionSPI {
	private final RaycastHandler raycastHandler;
	private final LayerCollisionMatrix layerMatrix;
	private final NarrowPhaseDetector narrowPhase;
	private final CollisionState collisionState;

	/**
	 * Creates a new collision SPI instance.
	 */
	public CollisionService() {
		this.raycastHandler = new RaycastHandler();
		this.layerMatrix = new LayerCollisionMatrix();
		this.narrowPhase = new NarrowPhaseDetector(layerMatrix);
		this.collisionState = new CollisionState();
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance) {
		return raycastHandler.raycast(origin, direction, maxDistance);
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, PhysicsLayer layer) {
		return raycastHandler.raycast(origin, direction, maxDistance, layer);
	}

	@Override
	public RaycastHit raycast(Vector2D origin, Vector2D direction, float maxDistance, List<PhysicsLayer> layers) {
		return raycastHandler.raycast(origin, direction, maxDistance, layers);
	}

	@Override
	public boolean isPointInCollider(Vector2D point) {
		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Check each collider
		for (ColliderNode node : colliderNodes) {
			if (!NodeValidator.isColliderNodeValid(node)) {
				continue;
			}

			if (isPointInCollider(point, node.getEntity())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isPointInCollider(Vector2D point, Entity entity) {
		if (entity == null) {
			return false;
		}

		ColliderComponent collider = entity.getComponent(ColliderComponent.class);
		if (collider == null || !collider.isEnabled()) {
			return false;
		}

		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform == null) {
			return false;
		}

		// Calculate world position of the collider
		Vector2D worldPos = transform.getPosition().add(collider.getOffset());

		// Get the shape
		var shape = collider.getShape();

		// Check if the point is inside the shape
		if (shape instanceof CircleShape) {
			CircleShape circle = (CircleShape) shape;
			float radius = circle.getRadius();

			// Calculate squared distance to center
			float dx = point.x() - worldPos.x();
			float dy = point.y() - worldPos.y();
			float distSquared = dx * dx + dy * dy;

			// Inside if distance is less than radius
			return distSquared <= radius * radius;
		}
		else if (shape instanceof BoxShape) {
			BoxShape box = (BoxShape) shape;
			float width = box.getWidth();
			float height = box.getHeight();

			// Check if the point is inside the box
			return point.x() >= worldPos.x() && point.x() <= worldPos.x() + width &&
				point.y() >= worldPos.y() && point.y() <= worldPos.y() + height;
		}

		// Unknown shape type
		return false;
	}

	@Override
	public boolean isPositionValid(Entity entity, Vector2D proposedPosition, CollisionOptions options) {
		if (entity == null) {
			return true;
		}

		ColliderComponent collider = entity.getComponent(ColliderComponent.class);
		if (collider == null || !collider.isEnabled()) {
			return true; // No collider, position is valid
		}

		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform == null) {
			return true; // No transform, position is valid
		}

		// Store original position
		Vector2D originalPosition = transform.getPosition();

		try {
			// Set temporary position for collision check
			transform.setPosition(proposedPosition);

			// Check collisions with all other colliders
			Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

			for (ColliderNode node : colliderNodes) {
				// Skip self
				if (node.getEntity() == entity) {
					continue;
				}

				// Skip invalid nodes
				if (!NodeValidator.isColliderNodeValid(node)) {
					continue;
				}

				// Skip ignored layers
				if (options.shouldIgnoreLayer(node.collider.getLayer())) {
					continue;
				}

				// Skip if layers can't collide
				if (!layerMatrix.canLayersCollide(collider.getLayer(), node.collider.getLayer())) {
					continue;
				}

				// Skip triggers during movement validation
				if (node.collider.isTrigger()) {
					continue;
				}

				// Check if other entity has physics (is dynamic)
				boolean otherIsDynamic = node.getEntity().hasComponent(PhysicsComponent.class);

				// Skip dynamic-dynamic collisions if configured to allow them
				if (otherIsDynamic && !options.shouldPreventDynamicCollisions()) {
					continue;
				}

				// Skip static-static collisions if configured to allow them
				if (!otherIsDynamic && !options.shouldPreventStaticCollisions()) {
					continue;
				}

				// Check for collision
				ContactPoint contact = narrowPhase.checkCollision(collider, node.collider);
				if (contact != null) {
					if (options.isTriggerEvents()) {
						if (collider.isTrigger()) {
							EventSystem.getInstance().publish(new TriggerEnterEvent(entity, node.getEntity()));
						} else {
							EventSystem.getInstance().publish(new CollisionEnterEvent(entity, node.getEntity(), contact));
						}
					}

					return false; // Collision detected, position is invalid
				}
			}

			// No collisions found
			return true;
		} finally {
			// Restore original position
			transform.setPosition(originalPosition);
		}
	}

	@Override
	public List<Entity> overlapCircle(Vector2D center, float radius) {
		return overlapCircle(center, radius, null);
	}

	@Override
	public List<Entity> overlapCircle(Vector2D center, float radius, PhysicsLayer layer) {
		List<Entity> result = new ArrayList<>();

		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		System.out.println("colliderNodes size: " + colliderNodes.size() + "\n");

		for (ColliderNode node : colliderNodes) {
			System.out.println("Entity: " + node.getEntity().getID() + "\n");
		}

		// Filter valid nodes
		var validNodes = colliderNodes.stream()
			.filter(NodeValidator::isColliderNodeValid)
			.toList();

		// Create a temporary circle shape for overlap tests
		CircleShape circleShape = new CircleShape(radius);

		// Check each collider
		for (ColliderNode node : validNodes) {
			// Skip if filtering by layer and layer doesn't match
			if (layer != null && node.collider.getLayer() != layer) {
				continue;
			}

			System.out.println("Entity passed filtering: " + node.getEntity().getID() + "\n");

			// Get shape and position
			var shape = node.collider.getShape();
			var pos = node.transform.getPosition().add(node.collider.getOffset());

			System.out.println("NarrowPhase: " + narrowPhase);

			// Check for overlap
			ContactPoint contact = narrowPhase.testShapeCollision(circleShape, center, shape, pos);
			System.out.println("Contact: " + contact.toString());
			if (contact != null) {
				result.add(node.getEntity());
				System.out.println("Result was added for Entity: " + node.getEntity() + "\n");
			}
		}

		return result;
	}

	@Override
	public List<Entity> overlapBox(Vector2D center, float width, float height) {
		return overlapBox(center, width, height, null);
	}

	@Override
	public List<Entity> overlapBox(Vector2D center, float width, float height, PhysicsLayer layer) {
		List<Entity> result = new ArrayList<>();

		// Get all collider nodes
		Set<ColliderNode> colliderNodes = NodeManager.active().getNodes(ColliderNode.class);

		// Filter valid nodes
		var validNodes = colliderNodes.stream()
			.filter(NodeValidator::isColliderNodeValid)
			.toList();

		// Create a temporary box shape for overlap tests
		BoxShape boxShape = new BoxShape(width, height);

		// Adjust center to top-left corner (box shape uses top-left origin)
		Vector2D topLeft = new Vector2D(
			center.x() - width / 2,
			center.y() - height / 2
		);

		// Check each collider
		for (ColliderNode node : validNodes) {
			// Skip if filtering by layer and layer doesn't match
			if (layer != null && node.collider.getLayer() != layer) {
				continue;
			}

			// Get shape and position
			var shape = node.collider.getShape();
			var pos = node.transform.getPosition().add(node.collider.getOffset());

			// Check for overlap
			ContactPoint contact = narrowPhase.testShapeCollision(boxShape, topLeft, shape, pos);
			if (contact != null) {
				result.add(node.getEntity());
			}
		}

		return result;
	}

	@Override
	public void cleanupEntity(Entity entity) {
		if (entity != null) {
			collisionState.markForCleanup(entity);
		}
	}
}