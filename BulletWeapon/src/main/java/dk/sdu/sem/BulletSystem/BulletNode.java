package dk.sdu.sem.BulletSystem;
import java.util.Set;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;

import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

public class BulletNode extends Node implements INodeProvider<BulletNode> {
    public TransformComponent transformComponent;
    public BulletComponent bulletComponent;

    @Override
    public void initialize(Entity entity) {
        super.initialize(entity);

        transformComponent = entity.getComponent(TransformComponent.class);
        bulletComponent = entity.getComponent(BulletComponent.class);
    }

    @Override
    public Set<Class<? extends IComponent>> getRequiredComponents() {
        return Set.of(TransformComponent.class, BulletComponent.class);
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