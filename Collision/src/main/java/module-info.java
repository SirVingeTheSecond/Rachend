module Collision {
	uses dk.sdu.sem.collision.ITriggerEventSPI;
	uses dk.sdu.sem.collision.ITriggerCollisionListener;
	uses dk.sdu.sem.collision.ICollisionSPI;
	requires GameEngine;
	requires javafx.graphics;
	requires CommonPlayer;
	requires CommonItem;
	requires CommonCollision;
	requires Common;
	requires java.logging;

	provides dk.sdu.sem.collision.ICollisionSPI with
		dk.sdu.sem.collisionsystem.CollisionSystem;

	provides dk.sdu.sem.collision.IColliderFactory with
		dk.sdu.sem.collisionsystem.ColliderFactory;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.collisionsystem.ColliderNode,
		dk.sdu.sem.collisionsystem.PhysicsColliderNode,
		dk.sdu.sem.collisionsystem.TilemapColliderNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.collisionsystem.ColliderNodeProvider,
		dk.sdu.sem.collisionsystem.PhysicsColliderNodeProvider,
		dk.sdu.sem.collisionsystem.TilemapColliderNodeProvider;

	provides dk.sdu.sem.gamesystem.services.IFixedUpdate with
		dk.sdu.sem.collisionsystem.CollisionSystem;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.collisionsystem.CollisionDebugRenderer;

	provides dk.sdu.sem.gamesystem.services.IStart with
		dk.sdu.sem.collisionsystem.TriggerSystem;

	provides dk.sdu.sem.collision.ITriggerCollisionListener with
		dk.sdu.sem.collisionsystem.TriggerSystem;

	exports dk.sdu.sem.collisionsystem;
}