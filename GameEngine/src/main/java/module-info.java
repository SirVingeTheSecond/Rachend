module GameEngine {
	requires Common;
	requires CommonCollision;
	requires javafx.graphics;
	requires java.desktop;

	exports dk.sdu.sem.gamesystem;
	exports dk.sdu.sem.gamesystem.services;
	exports dk.sdu.sem.gamesystem.data;
	exports dk.sdu.sem.gamesystem.components;
	exports dk.sdu.sem.gamesystem.nodes;
	exports dk.sdu.sem.gamesystem.systems;

	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.gamesystem.services.IUpdate;
	uses dk.sdu.sem.gamesystem.services.ILateUpdate;
	uses dk.sdu.sem.gamesystem.services.IFixedUpdate;
	uses dk.sdu.sem.gamesystem.nodes.INode;
	uses dk.sdu.sem.gamesystem.systems.ISystem;

	provides dk.sdu.sem.gamesystem.nodes.INode with dk.sdu.sem.gamesystem.data.RenderNode;
}
