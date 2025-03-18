package dk.sdu.sem.physicssystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class PhysicsNode extends Node {

	public TransformComponent transform;
	public PhysicsComponent physicsComponent;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		physicsComponent = entity.getComponent(PhysicsComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(TransformComponent.class, PhysicsComponent.class);
	}
}
