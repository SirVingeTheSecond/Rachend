package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonstats.StatsComponent;

import java.util.Set;

public class EnemyNode extends Node {

	public TransformComponent transform;
	public EnemyComponent enemy;
	public PhysicsComponent physics;
	public StatsComponent stats;
	public WeaponComponent weapon;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		this.transform = entity.getComponent(TransformComponent.class);
		this.enemy = entity.getComponent(EnemyComponent.class);
		this.physics = entity.getComponent(PhysicsComponent.class);
		this.stats = entity.getComponent(StatsComponent.class);
		this.weapon = entity.getComponent(WeaponComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(
			TransformComponent.class,
			EnemyComponent.class,
			PhysicsComponent.class,
			StatsComponent.class,
			WeaponComponent.class
		);
	}
}