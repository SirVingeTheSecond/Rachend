import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collisionsystem.GridCollisionService;

module Collision {
	requires Common;
	requires CommonCollision;
	requires GameEngine;
	provides ICollisionSPI with GridCollisionService;
}
