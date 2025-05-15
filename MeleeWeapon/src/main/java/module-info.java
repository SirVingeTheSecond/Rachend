import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.IMeleeWeaponSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.services.IUpdate;

module MeleeWeapon {
	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.commonsystem.debug.IDebugDrawManager;

	requires CommonWeapon;
	requires CommonStats;
	requires GameEngine;
	requires CommonEnemy;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;
	requires javafx.graphics;
	requires Collision;

	provides IWeaponSPI with dk.sdu.sem.meleeweaponsystem.MeleeWeapon;
	provides IMeleeWeaponSPI with dk.sdu.sem.meleeweaponsystem.MeleeWeapon;
	provides IAssetProvider with dk.sdu.sem.meleeweaponsystem.MeleeAssetProvider;
	provides IUpdate with dk.sdu.sem.meleeweaponsystem.MeleeSystem;

	provides dk.sdu.sem.commonsystem.Node with dk.sdu.sem.meleeweaponsystem.MeleeEffectNode;
	provides dk.sdu.sem.commonsystem.INodeProvider with dk.sdu.sem.meleeweaponsystem.MeleeEffectNode;

	exports dk.sdu.sem.meleeweaponsystem;
}