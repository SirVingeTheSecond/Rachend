import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collisionsystem.GridCollisionService;

module Collision {
	requires Common;
	requires CommonCollision;
	requires GameEngine;
	requires java.desktop;
	provides ICollisionSPI with GridCollisionService;
}
