module BulletWeapon {
	requires CommonWeapon;
	requires GameEngine;
	requires Common;
	requires javafx.graphics;

	exports dk.sdu.sem.BulletSystem;
//	provides dk.sdu.sem.commonweapon.IWeapon with BulletWeapon;

}