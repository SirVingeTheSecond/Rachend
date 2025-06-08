import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

module TestEnvironment {
	uses dk.sdu.sem.commonweapon.IWeaponSPI;
	uses dk.sdu.sem.commonweapon.IBulletFactory;
	requires CommonWeapon;
	requires CommonStats;
	requires GameEngine;
	requires Common;
	requires javafx.graphics;

	provides IUpdate with dk.sdu.sem.testing.TestSystem;
	provides IGUIUpdate with dk.sdu.sem.testing.TestSystem;
}