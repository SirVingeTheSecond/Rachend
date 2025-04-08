package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonsystem.INodeProvider;

public class ItemNodeProvider implements INodeProvider<ItemNode> {
	@Override
	public Class<ItemNode> getNodeType() {
		return ItemNode.class;
	}

	@Override
	public ItemNode create() {
		return new ItemNode();
	}
}