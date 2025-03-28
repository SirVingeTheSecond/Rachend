package dk.sdu.sem.inventory;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.Set;

/**
 * System for processing inventory-related logic.
 */
public class InventorySystem implements IUpdate {

	@Override
	public void update() {
		Set<PlayerInventoryNode> inventoryNodes = NodeManager.active().getNodes(PlayerInventoryNode.class);

		// Do something
	}
}