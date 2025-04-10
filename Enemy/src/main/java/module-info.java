import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

module Enemy {
	requires CommonHealth;
	requires CommonWeapon;
	requires Pathfinding;
	requires GameEngine;
	requires CommonEnemy;
	requires CommonPlayer;
	requires Common;
	uses IWeaponSPI;

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