import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.meleeweaponsystem.MeleeAssetProvider;
import dk.sdu.sem.meleeweaponsystem.MeleeWeapon;

module MeleeWeapon {
	uses dk.sdu.sem.collision.ICollisionSPI;
	requires CommonWeapon;
	requires Collision;
    requires CommonHealth;
	requires CommonStats;
	requires GameEngine;
	requires CommonEnemy;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;
	requires com.fasterxml.jackson.annotation;
	requires javafx.graphics;
	provides IWeaponSPI with MeleeWeapon;
	provides IAssetProvider with MeleeAssetProvider;
}