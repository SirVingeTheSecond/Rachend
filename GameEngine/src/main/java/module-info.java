import dk.sdu.sem.commonsystem.Node;

module GameEngine {
	requires Common;
	requires CommonCollision;
	requires javafx.graphics;
	requires java.desktop;

	exports dk.sdu.sem.gamesystem;
	exports dk.sdu.sem.gamesystem.services;
	exports dk.sdu.sem.gamesystem.data;
	exports dk.sdu.sem.gamesystem.components;
	exports dk.sdu.sem.gamesystem.scenes;
	exports dk.sdu.sem.gamesystem.input;

	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.gamesystem.services.IUpdate;
	uses dk.sdu.sem.gamesystem.services.ILateUpdate;
	uses dk.sdu.sem.gamesystem.services.IFixedUpdate;
	uses Node;
	uses dk.sdu.sem.gamesystem.services.IStart;

	provides Node with dk.sdu.sem.gamesystem.data.RenderNode;
}
