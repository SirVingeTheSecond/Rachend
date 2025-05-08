package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonitem.ItemDropComponent;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.*;

import java.util.Set;

public class ItemDropNode extends Node implements INodeProvider<ItemDropNode> {
	public TransformComponent transform;
	public StatsComponent stats;
	public ItemDropComponent drop;


	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		stats = entity.getComponent(StatsComponent.class);
		drop = entity.getComponent(ItemDropComponent.class);
		ItemDropSystem.registerDropNode(this);
	}

	@Override
	public Class<ItemDropNode> getNodeType() {
		return ItemDropNode.class;
	}

	@Override
	public ItemDropNode create() {
		return new ItemDropNode();
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(StatsComponent.class, TransformComponent.class, ItemDropComponent.class);
	}
}
