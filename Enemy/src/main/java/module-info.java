import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

module Enemy {
	requires GameEngine;
	requires CommonHealth;
	requires CommonWeapon;
	requires CommonPathfinding;
	requires CommonEnemy;
	requires CommonPlayer;
	requires CommonStats;
	requires CommonInventory;
	requires CommonItem;
	requires CommonCollision;
	requires Common;
    requires Collision;
	requires javafx.graphics;

	uses IWeaponSPI;
	uses dk.sdu.sem.collision.IColliderFactory;
	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.commonpathfinding.IPathfindingSPI;
	uses dk.sdu.sem.commonsystem.debug.IDebugDrawManager;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.enemysystem.EnemyNode,
		dk.sdu.sem.enemysystem.PlayerTargetNode;

	provides dk.sdu.sem.enemy.IEnemyFactory
		with dk.sdu.sem.enemysystem.EnemyFactory;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.enemysystem.EnemyAnimationController,
		dk.sdu.sem.enemysystem.EnemySystem;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.enemysystem.EnemyNodeProvider,
		dk.sdu.sem.enemysystem.PlayerTargetNode;

	provides IAssetProvider
		with dk.sdu.sem.enemysystem.EnemyAssetProvider;

	exports dk.sdu.sem.enemysystem;
}