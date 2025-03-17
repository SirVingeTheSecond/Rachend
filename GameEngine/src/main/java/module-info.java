import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.gamesystem.services.*;

module GameEngine {
	exports dk.sdu.sem.gamesystem.services;
	exports dk.sdu.sem.gamesystem;
	exports dk.sdu.sem.gamesystem.data;
	exports dk.sdu.sem.gamesystem.components;
	exports dk.sdu.sem.gamesystem.nodes;
	exports dk.sdu.sem.gamesystem.systems;

	requires Common;
	requires CommonCollision;
	requires javafx.graphics;
	requires java.desktop;

	uses ICollisionSPI;
	uses IUpdate;
	uses ILateUpdate;
	uses IFixedUpdate;
	uses INode;
	uses ISystem;

	provides Node with RenderNode;
}