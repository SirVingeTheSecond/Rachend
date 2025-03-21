module GameEngine {
	requires CommonCollision;
	requires CommonPlayer;
	requires Common;
	requires javafx.graphics;
	requires java.desktop;

	exports dk.sdu.sem.gamesystem;
	exports dk.sdu.sem.gamesystem.services;
	exports dk.sdu.sem.gamesystem.data;
	exports dk.sdu.sem.gamesystem.components;
	exports dk.sdu.sem.gamesystem.scenes;
	exports dk.sdu.sem.gamesystem.input;
	exports dk.sdu.sem.gamesystem.rendering;
	exports dk.sdu.sem.gamesystem.factories;
	exports dk.sdu.sem.gamesystem.animation;
	exports dk.sdu.sem.gamesystem.assets.registry;
	exports dk.sdu.sem.gamesystem.assets;

	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.commonsystem.Node;
	uses dk.sdu.sem.commonsystem.INodeProvider;
	uses dk.sdu.sem.gamesystem.services.IUpdate;
	uses dk.sdu.sem.gamesystem.services.ILateUpdate;
	uses dk.sdu.sem.gamesystem.services.IFixedUpdate;
	uses dk.sdu.sem.gamesystem.factories.IEntityFactory;
	uses dk.sdu.sem.gamesystem.rendering.IRenderSystem;
	uses dk.sdu.sem.player.IPlayerFactory;
	uses dk.sdu.sem.gamesystem.assets.IAssetLoader;
	uses dk.sdu.sem.gamesystem.assets.registry.IAssetRegistryProvider;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.gamesystem.data.SpriteNode,
		dk.sdu.sem.gamesystem.data.TileMapNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.gamesystem.data.SpriteNodeProvider,
		dk.sdu.sem.gamesystem.data.TileMapNodeProvider;

	provides dk.sdu.sem.gamesystem.services.ILateUpdate with
		dk.sdu.sem.gamesystem.rendering.FXRenderSystem;

	provides dk.sdu.sem.gamesystem.rendering.IRenderSystem with
		dk.sdu.sem.gamesystem.rendering.FXRenderSystem;

	provides dk.sdu.sem.gamesystem.factories.IEntityFactory with
		dk.sdu.sem.gamesystem.factories.TileMapFactory;
}