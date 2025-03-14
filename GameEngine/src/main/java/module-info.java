import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.gamesystem.services.*;

module GameEngine {
	exports dk.sdu.sem.gamesystem.services;
	exports dk.sdu.sem.gamesystem;
	exports dk.sdu.sem.gamesystem.data;
	exports dk.sdu.sem.gamesystem.components;

	requires Common;
	requires CommonCollision;
	requires javafx.graphics;

	uses ICollisionSPI;
	uses IUpdate;
	uses ILateUpdate;
	uses IFixedUpdate;
}