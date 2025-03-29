package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

public class WeaponPlayerNode extends Node implements INodeProvider<WeaponPlayerNode> {
	public PlayerComponent player;
	public TransformComponent playerTransform;
	public WeaponComponent weapon;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);

		player = entity.getComponent(PlayerComponent.class);
		playerTransform = entity.getComponent(TransformComponent.class);
		weapon = entity.getComponent(WeaponComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PlayerComponent.class, WeaponComponent.class, TransformComponent.class);
	}

	@Override
	public Class<WeaponPlayerNode> getNodeType() {
		return WeaponPlayerNode.class;
	}

	@Override
	public WeaponPlayerNode create() {
		return new WeaponPlayerNode();
	}
}