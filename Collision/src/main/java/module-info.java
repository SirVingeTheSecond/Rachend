import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collisionsystem.GridCollisionService;

module Collision {
	exports dk.sdu.sem.collisionsystem.components to GameEngine;

	requires java.logging;
	requires GameEngine;

	provides ICollisionSPI with GridCollisionService;
}