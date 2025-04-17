module CommonWeapon {
	uses dk.sdu.sem.collision.IColliderFactory;
	exports dk.sdu.sem.commonweapon;
	requires Item;
	requires CommonStats;
	requires GameEngine;
    requires CommonItem;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;
}