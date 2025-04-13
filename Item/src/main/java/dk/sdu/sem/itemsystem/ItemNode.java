package dk.sdu.sem.itemsystem;

import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonitem.ItemComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.Set;

/**
 * Node for entities that represent collectible items.
 */
public class ItemNode extends Node implements INodeProvider<ItemNode> {
	// Data components
	public TransformComponent transform;
	public SpriteRendererComponent spriteRenderer;
	public ItemComponent item;           // Data about what the item is

	// Behavior components
	public PickupComponent pickup;       // Behaviour when collected
	public ColliderComponent collider;   // For trigger detection

	@Override
	public void initialize(Entity entity) {
		super.initialize(entity);
		transform = entity.getComponent(TransformComponent.class);
		spriteRenderer = entity.getComponent(SpriteRendererComponent.class);
		item = entity.getComponent(ItemComponent.class);
		pickup = entity.getComponent(PickupComponent.class);
		collider = entity.getComponent(ColliderComponent.class);
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of(
			TransformComponent.class,
			SpriteRendererComponent.class,
			ItemComponent.class,
			PickupComponent.class,
			ColliderComponent.class
		);
	}

	@Override
	public Class<ItemNode> getNodeType() {
		return ItemNode.class;
	}

	@Override
	public ItemNode create() {
		return new ItemNode();
	}
}