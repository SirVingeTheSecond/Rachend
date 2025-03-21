module Enemy {
	requires CommonEnemy;
	requires GameEngine;
	requires Common;

	provides dk.sdu.sem.commonsystem.Node
		with dk.sdu.sem.enemysystem.EnemyNode;

	exports dk.sdu.sem.enemysystem;
}