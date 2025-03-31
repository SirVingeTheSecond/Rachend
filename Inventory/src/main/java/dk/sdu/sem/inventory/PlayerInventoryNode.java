package dk.sdu.sem.inventory;

import dk.sdu.sem.commoninventory.InventoryComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

/**
 * Node for entities that have both player and inventory components.
 */
public class PlayerInventoryNode extends Node {
	public PlayerComponent player;
	public InventoryComponent inventory;

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		player = entity.getComponent(PlayerComponent.class);
		inventory = entity.getComponent(InventoryComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(PlayerComponent.class, InventoryComponent.class);
	}
}