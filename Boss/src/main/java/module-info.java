import dk.sdu.sem.commonlevel.room.IRoomCreatedListener;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;

module Boss {
	uses dk.sdu.sem.commonweapon.IBulletFactory;
	uses dk.sdu.sem.enemy.IEnemyFactory;

	requires GameEngine;
	requires Common;
	requires CommonCollision;
	requires CommonEnemy;
	requires CommonLevel;
	requires CommonPathfinding;
	requires CommonPlayer;
	requires CommonStats;
	requires CommonWeapon;
	requires javafx.graphics;


	provides IWeaponSPI with dk.sdu.sem.boss.BossWeapon;
	provides IAssetProvider with dk.sdu.sem.boss.BossAssetProvider;
	provides IRoomCreatedListener with dk.sdu.sem.boss.BossRoom;
	provides Node with dk.sdu.sem.boss.BossNode;
	provides INodeProvider with dk.sdu.sem.boss.BossNode;
	provides IUpdate with dk.sdu.sem.boss.BossSystem;
	provides IStart with dk.sdu.sem.boss.BossSystem;
	provides IGUIUpdate with dk.sdu.sem.boss.BossBar;
}