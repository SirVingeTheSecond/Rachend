package dk.sdu.sem.bulletweapon;

import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.commonweapon.BulletComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;

import java.util.Set;

/**
 * Node for accessing projectile components.
 */
public class BulletNode extends Node implements INodeProvider<BulletNode> {
	public TransformComponent transform;
	public BulletComponent bullet;
	public PhysicsComponent physics;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		bullet = entity.getComponent(BulletComponent.class);
		physics = entity.getComponent(PhysicsComponent.class);
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