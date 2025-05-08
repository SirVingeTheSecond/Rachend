import dk.sdu.sem.itemsystem.consumableitems.*;
import dk.sdu.sem.itemsystem.passiveitems.*;

module Item {
	requires Collision;
	requires CommonStats;
	requires GameEngine;
	requires CommonItem;
	requires java.logging;
	requires CommonPlayer;
	requires CommonCollision;
	requires CommonInventory;
	requires Common;

	uses dk.sdu.sem.collision.IColliderFactory;

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

	provides dk.sdu.sem.commonitem.IItem with
		Apple,
		Coin,
		EnergyPotion,
		GreaterHealthPotion,
		HealthPotion,
		StrengthPotion,
		AtkSpeedUpper,
		BiggerBullets,
		Cup,
		DamageUpper,
		FasterBullets,
		HealthUpper,
		KnockbackUpper,
		SpeedUpper;

	exports dk.sdu.sem.itemsystem;
	exports dk.sdu.sem.itemsystem.passiveitems;
}