module CommonCollision {
	requires Common;
	requires CommonTilemap;

	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.collision.events.IEventSystem;
	uses dk.sdu.sem.collision.IColliderFactory;
	uses dk.sdu.sem.collision.IDebugVisualizationSPI;

	exports dk.sdu.sem.collision;
	exports dk.sdu.sem.collision.components;
	exports dk.sdu.sem.collision.shapes;
	exports dk.sdu.sem.collision.events;
	exports dk.sdu.sem.collision.data;
}