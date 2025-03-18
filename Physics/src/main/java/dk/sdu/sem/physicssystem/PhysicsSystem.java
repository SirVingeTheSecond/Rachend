package dk.sdu.sem.physicssystem;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;

public class PhysicsSystem implements IFixedUpdate {

	@Override
	public void fixedUpdate() {
		//Add static instances for active scene and NodeManager to simplify this call
		SceneManager.getInstance().getActiveScene().getNodeManager().getNodes(PhysicsNode.class).forEach(node -> {
			Vector2D velocity = node.physicsComponent.getVelocity();

			Vector2D friction = velocity
				.normalize()
				.scale(node.physicsComponent.getFriction() * (float)Time.getFixedDeltaTime());

			node.physicsComponent.setVelocity(velocity.subtract(friction));

			Vector2D pos = node.transform.getPosition();
			pos = pos.add(node.physicsComponent.getVelocity().scale((float)Time.getFixedDeltaTime()));
			node.transform.setPosition(pos);
		});
	}
}
