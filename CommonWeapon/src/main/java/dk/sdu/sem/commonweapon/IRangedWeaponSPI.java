package dk.sdu.sem.commonweapon;

public interface IRangedWeaponSPI extends IWeaponSPI {
	float getBulletSpeed();

	// Should be in IWeaponSPI but limited to IRangedWeaponSPI for now
	float getAttackKnockback();
}