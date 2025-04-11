import dk.sdu.sem.meleeweaponsystem.MeleeWeapon;

module MeleeWeapon {
	requires CommonWeapon;
	requires Collision;
	requires GameEngine;
	requires CommonCollision;
//	requires CommonHealth;
	requires CommonHealth;
	requires Common;
	provides dk.sdu.sem.commonweaponsystem.IWeaponSPI with MeleeWeapon;
}