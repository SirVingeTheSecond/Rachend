module Collision {
	uses dk.sdu.sem.collision.events.IEventSystem;
	requires javafx.graphics;
	requires java.logging;
	requires GameEngine;
	requires CommonPlayer;
	requires CommonItem;
	requires CommonCollision;
	requires CommonTilemap;
	requires Common;

	provides dk.sdu.sem.collision.ICollisionSPI with
		dk.sdu.sem.collisionsystem.CollisionService;

	provides dk.sdu.sem.collision.events.IEventSystem with
		dk.sdu.sem.collisionsystem.events.EventSystem;

	provides dk.sdu.sem.collision.IColliderFactory with
		dk.sdu.sem.collisionsystem.ColliderFactory;

	provides dk.sdu.sem.gamesystem.services.IFixedUpdate with
		dk.sdu.sem.collisionsystem.systems.CollisionSystem;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.collisionsystem.debug.CollisionDebugRenderer;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.collisionsystem.nodes.ColliderNode,
		dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode,
		dk.sdu.sem.collisionsystem.nodes.PhysicsColliderNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.collisionsystem.nodes.ColliderNode,
		dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode,
		dk.sdu.sem.collisionsystem.nodes.PhysicsColliderNode;

	exports dk.sdu.sem.collisionsystem;
	exports dk.sdu.sem.collisionsystem.debug;
	exports dk.sdu.sem.collisionsystem.raycasting;
	exports dk.sdu.sem.collisionsystem.events;
	exports dk.sdu.sem.collisionsystem.nodes;
	exports dk.sdu.sem.collisionsystem.utils;
	exports dk.sdu.sem.collisionsystem.systems;
	exports dk.sdu.sem.collisionsystem.state;
}