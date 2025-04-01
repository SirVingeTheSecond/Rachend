package dk.sdu.sem.weaponsystem;
import java.util.Set;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;

public class WeaponNode extends Node implements INodeProvider<WeaponNode> {
	public WeaponComponent weapon;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);

		weapon = entity.getComponent(WeaponComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(WeaponComponent.class);
	}

	@Override
	public Class<WeaponNode> getNodeType() {
		return WeaponNode.class;
	}

	@Override
	public WeaponNode create() {
		return new WeaponNode();
	}
}