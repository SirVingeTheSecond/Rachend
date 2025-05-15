module CommonWeapon {
	uses dk.sdu.sem.collision.IColliderFactory;
	uses dk.sdu.sem.commonweapon.IWeaponSPI;
	uses dk.sdu.sem.commonweapon.IRangedWeaponSPI;
	uses dk.sdu.sem.commonweapon.IMeleeWeaponSPI;

	exports dk.sdu.sem.commonweapon;
	requires CommonStats;
	requires GameEngine;
    requires CommonItem;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;
}