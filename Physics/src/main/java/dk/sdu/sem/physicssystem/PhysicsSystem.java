package dk.sdu.sem.physicssystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

public class PhysicsSystem implements IFixedUpdate, IUpdate {

	@Override
	public void fixedUpdate() {
		//Add static instances for active scene and NodeManager to simplify this call
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
		NodeManager.active().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D pos = node.transform.getPosition();
			pos = pos.add(node.physicsComponent.getVelocity().scale((float) Time.getDeltaTime()));
			node.transform.setPosition(pos);
		});
	}
}
