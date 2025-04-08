module Inventory {
	requires Common;
	requires CommonInventory;
	requires GameEngine;
	requires CommonPlayer;
	requires javafx.graphics;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.inventory.PlayerInventoryNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.inventory.PlayerInventoryNodeProvider;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.inventory.InventorySystem;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.inventory.InventoryDisplay;

	exports dk.sdu.sem.inventory;
}