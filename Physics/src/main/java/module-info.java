import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.physicssystem.PhysicsNode;

module Physics {
	requires Common;
	requires GameEngine;

	provides Node with PhysicsNode;
}