import dk.sdu.sem.collision.ICollisionSPI;

module GameEngine {
	exports dk.sdu.sem.gamesystem;
	exports dk.sdu.sem.gamesystem.data;

	requires Common;
	requires CommonCollision;
	requires javafx.graphics;

	uses ICollisionSPI;
}