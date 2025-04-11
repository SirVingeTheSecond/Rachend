import dk.sdu.sem.commonweaponsystem.IBulletWeapon;

module BulletWeapon {
	requires CommonWeapon;
	requires GameEngine;
	requires Common;
	requires javafx.graphics;

	exports dk.sdu.sem.BulletSystem;
	provides IBulletWeapon with dk.sdu.sem.BulletSystem.BulletWeapon;
	provides dk.sdu.sem.gamesystem.services.IUpdate with dk.sdu.sem.BulletSystem.BulletSystem;
	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with dk.sdu.sem.BulletSystem.BulletSystem;

	provides dk.sdu.sem.commonsystem.Node with dk.sdu.sem.BulletSystem.BulletNode;
	provides dk.sdu.sem.commonsystem.INodeProvider with dk.sdu.sem.BulletSystem.BulletNode;

}