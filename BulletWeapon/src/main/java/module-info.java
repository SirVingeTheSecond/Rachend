import dk.sdu.sem.commonweaponsystem.IWeaponSPI;

module BulletWeapon {
	requires CommonWeapon;
	requires CommonStats;
	requires GameEngine;
	requires javafx.graphics;
	requires CommonPlayer;
	requires CommonEnemy;
	requires CommonCollision;
	requires Common;

	exports dk.sdu.sem.BulletSystem;
	provides IWeaponSPI with dk.sdu.sem.BulletSystem.BulletWeapon;
	provides dk.sdu.sem.gamesystem.services.IUpdate with dk.sdu.sem.BulletSystem.BulletSystem;
	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with dk.sdu.sem.BulletSystem.BulletSystem;

	provides dk.sdu.sem.commonsystem.Node with dk.sdu.sem.BulletSystem.BulletNode;
	provides dk.sdu.sem.commonsystem.INodeProvider with dk.sdu.sem.BulletSystem.BulletNode;

	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with dk.sdu.sem.BulletSystem.BulletAssetProvider;
}