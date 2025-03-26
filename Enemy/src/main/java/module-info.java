import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

module Enemy {
	requires CommonEnemy;
	requires GameEngine;
	requires CommonHealth;
	requires Common;
	requires Player;

	provides dk.sdu.sem.commonsystem.Node
		with dk.sdu.sem.enemysystem.EnemyNode;

	provides dk.sdu.sem.enemy.IEnemyFactory
		with dk.sdu.sem.enemysystem.EnemyFactory;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.enemysystem.EnemyAnimationController,
		dk.sdu.sem.enemysystem.EnemySystem;

	provides dk.sdu.sem.commonsystem.INodeProvider
		with dk.sdu.sem.enemysystem.EnemyNodeProvider;

	provides IAssetProvider
		with dk.sdu.sem.enemysystem.EnemyAssetProvider;

	exports dk.sdu.sem.enemysystem;
}