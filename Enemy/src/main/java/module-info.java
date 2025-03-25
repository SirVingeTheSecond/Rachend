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

	exports dk.sdu.sem.enemysystem;
}