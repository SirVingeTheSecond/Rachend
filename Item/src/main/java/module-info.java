module Item {
	requires CommonInventory;
	requires GameEngine;
	requires CommonItem;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;

	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with
		dk.sdu.sem.itemsystem.ItemAssetProvider;

	provides dk.sdu.sem.collision.ITriggerEventSPI with
		dk.sdu.sem.itemsystem.ItemTriggerHandler;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.itemsystem.ItemNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.itemsystem.ItemNodeProvider;

	provides dk.sdu.sem.commonitem.IItemFactory with
		dk.sdu.sem.itemsystem.ItemFactory;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.itemsystem.ItemSystem;

	exports dk.sdu.sem.itemsystem;
}