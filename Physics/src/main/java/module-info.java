module Physics {
    requires Collision;
	requires GameEngine;
	requires CommonCollision;
	requires Common;

	uses dk.sdu.sem.collision.ICollisionSPI;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.physicssystem.PhysicsNode;
	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.physicssystem.PhysicsNodeProvider;
	provides dk.sdu.sem.gamesystem.services.IFixedUpdate with
		dk.sdu.sem.physicssystem.PhysicsSystem;
	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.physicssystem.PhysicsSystem;

	exports dk.sdu.sem.physicssystem;
}