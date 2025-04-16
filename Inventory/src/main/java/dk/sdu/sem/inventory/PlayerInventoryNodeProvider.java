package dk.sdu.sem.inventory;

import dk.sdu.sem.commonsystem.INodeProvider;


public class PlayerInventoryNodeProvider implements INodeProvider<PlayerInventoryNode> {
	@Override
	public Class<PlayerInventoryNode> getNodeType() {
		return PlayerInventoryNode.class;
	}

	@Override
	public PlayerInventoryNode create() {
		return new PlayerInventoryNode();
	}
}