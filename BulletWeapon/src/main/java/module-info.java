import dk.sdu.sem.commonweapon.IWeaponSPI;

module BulletWeapon {
	requires CommonWeapon;
	requires CommonStats;
	requires GameEngine;
	requires CommonPlayer;
	requires CommonEnemy;
	requires CommonCollision;
	requires Common;

	requires javafx.graphics;
    requires java.logging;

	provides IWeaponSPI with dk.sdu.sem.bulletweapon.BulletWeapon;
	provides dk.sdu.sem.gamesystem.services.IUpdate with dk.sdu.sem.bulletweapon.BulletSystem;
	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with dk.sdu.sem.bulletweapon.BulletSystem;

	provides dk.sdu.sem.commonsystem.Node with dk.sdu.sem.bulletweapon.BulletNode;
	provides dk.sdu.sem.commonsystem.INodeProvider with dk.sdu.sem.bulletweapon.BulletNode;

	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with dk.sdu.sem.bulletweapon.BulletAssetProvider;

	exports dk.sdu.sem.bulletweapon;
}