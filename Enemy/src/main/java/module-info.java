import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

module Enemy {
	requires GameEngine;
	requires CommonHealth;
	requires CommonWeapon;
    requires CommonCollision;
	requires CommonStats;
	requires CommonEnemy;
	requires CommonPlayer;
	requires Common;
	requires java.logging;
	uses IWeaponSPI;
	uses dk.sdu.sem.collision.IColliderFactory;
	uses dk.sdu.sem.collision.ICollisionSPI;

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