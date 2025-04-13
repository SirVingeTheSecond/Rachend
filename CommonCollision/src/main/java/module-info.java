module CommonCollision {
	requires Common;
	requires CommonTilemap;

	uses dk.sdu.sem.collision.ICollisionSPI;

	exports dk.sdu.sem.collision;
	exports dk.sdu.sem.collision.shapes;
	exports dk.sdu.sem.collision.components;
}