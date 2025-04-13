module CommonWeapon {
	uses dk.sdu.sem.collision.IColliderFactory;
	exports dk.sdu.sem.commonweaponsystem;
	requires Item;
	requires CommonStats;
	requires GameEngine;
	requires CommonCollision;
	requires Common;
}