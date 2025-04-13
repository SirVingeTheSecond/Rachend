module Item {
	uses dk.sdu.sem.collision.IColliderFactory;
	requires Collision;
	requires CommonStats;
	requires GameEngine;
	requires CommonItem;
	requires java.logging;
	requires CommonPlayer;
	requires CommonCollision;
	requires CommonInventory;
	requires Common;

	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with
		dk.sdu.sem.itemsystem.ItemAssetProvider;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.itemsystem.ItemNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.itemsystem.ItemNode;

	provides dk.sdu.sem.commonitem.IItemFactory with
		dk.sdu.sem.itemsystem.ItemFactory;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.itemsystem.ItemSystem;

	exports dk.sdu.sem.itemsystem;
}