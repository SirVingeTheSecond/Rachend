package dk.sdu.sem.physicssystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

public class PhysicsSystem implements IFixedUpdate, IUpdate {
	private final Optional<ICollisionSPI> collisionService;

	public PhysicsSystem() {
		// Try to load collision service (will be empty if module is missing)
		Iterator<ICollisionSPI> services = ServiceLoader.load(ICollisionSPI.class).iterator();
		collisionService = services.hasNext() ? Optional.of(services.next()) : Optional.empty();

		if (collisionService.isPresent()) {
			System.out.println("Collision service loaded successfully");
		} else {
			System.out.println("No collision service available - physics will not check for collisions");
		}
	}

	@Override
	public void fixedUpdate() {
		// Apply friction to all physics objects (regardless of collision)
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D velocity = node.physicsComponent.getVelocity();
			if (velocity.magnitudeSquared() == 0)
				return;

			Vector2D friction = velocity
				.scale(node.physicsComponent.getFriction() * (float)Time.getFixedDeltaTime());

			Vector2D newVelocity = velocity.subtract(friction);
			if (newVelocity.magnitudeSquared() < 0.01)
				newVelocity = new Vector2D(0,0);

			node.physicsComponent.setVelocity(newVelocity);
		});
	}

	@Override
	public void update() {
		// Collision system handles collision detection and prevention in fixedUpdate,
		// so this only updates positions based on velocities that have already been adjusted
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D pos = node.transform.getPosition();
			pos = pos.add(node.physicsComponent.getVelocity().scale((float) Time.getDeltaTime()));
			node.transform.setPosition(pos);
		});
	}
}