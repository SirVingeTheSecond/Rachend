import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.physicssystem.PhysicsNode;
import dk.sdu.sem.commonsystem.INodeProvider;
import dk.sdu.sem.physicssystem.PhysicsNodeProvider;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.physicssystem.PhysicsSystem;
import dk.sdu.sem.gamesystem.services.IUpdate;

module Physics {
	requires GameEngine;
	requires CommonCollision;
	requires Common;

	uses dk.sdu.sem.collision.ICollisionSPI;

	provides Node with PhysicsNode;
	provides INodeProvider with PhysicsNodeProvider;
	provides IFixedUpdate with PhysicsSystem;
	provides IUpdate with PhysicsSystem;

	exports dk.sdu.sem.physicssystem;
}