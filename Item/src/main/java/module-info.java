import dk.sdu.sem.itemsystem.consumableitems.*;
import dk.sdu.sem.itemsystem.passiveitems.*;

module Item {
	requires Collision;
	requires CommonStats;
	requires GameEngine;
	requires CommonItem;
    requires CommonPlayer;
	requires CommonCollision;
	requires CommonInventory;
	requires Common;
    requires com.fasterxml.jackson.databind;
	requires java.logging;

	uses dk.sdu.sem.collision.IColliderFactory;

	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with
		dk.sdu.sem.itemsystem.ItemAssetProvider;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.itemsystem.ItemNode,
		dk.sdu.sem.itemsystem.ItemDropNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.itemsystem.ItemNode,
		dk.sdu.sem.itemsystem.ItemDropNode;

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
		HeavyArmor,
		HeavyBullets,
		KnockbackUpper,
		LightBullets,
		RapidFireConv,
		SpeedUpper;

	exports dk.sdu.sem.itemsystem;
	exports dk.sdu.sem.itemsystem.passiveitems;
}