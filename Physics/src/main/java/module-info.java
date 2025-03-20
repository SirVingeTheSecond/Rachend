import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.physicssystem.PhysicsSystem;
import dk.sdu.sem.physicssystem.PhysicsNode;

module Physics {
	requires Common;
	requires GameEngine;

	provides Node with PhysicsNode;
	provides IFixedUpdate with PhysicsSystem;
	provides IUpdate with PhysicsSystem;

	exports dk.sdu.sem.physicssystem;
}