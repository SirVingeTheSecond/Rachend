package dk.sdu.sem.BulletSystem;
import java.util.Set;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;

import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.commonsystem.TransformComponent;

public class BulletNode extends Node implements INodeProvider<BulletNode> {
    public TransformComponent transformComponent;
    public BulletComponent bulletComponent;
	public PhysicsComponent physicsComponent;

    @Override
    public void initialize(Entity entity) {
        super.initialize(entity);

        transformComponent = entity.getComponent(TransformComponent.class);
        bulletComponent = entity.getComponent(BulletComponent.class);
		physicsComponent = entity.getComponent(PhysicsComponent.class);
    }

    @Override
    public Set<Class<? extends IComponent>> getRequiredComponents() {
        return Set.of(TransformComponent.class, BulletComponent.class, PhysicsComponent.class);
    }

    @Override
    public Class<BulletNode> getNodeType() {
        return BulletNode.class;
    }

    @Override
    public BulletNode create() {
        return new BulletNode();
    }


}