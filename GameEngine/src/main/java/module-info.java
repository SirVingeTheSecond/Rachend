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
	exports dk.sdu.sem.gamesystem.assets;
	exports dk.sdu.sem.gamesystem.assets.providers;

	uses dk.sdu.sem.collision.ICollisionSPI;
	uses dk.sdu.sem.commonsystem.Node;
	uses dk.sdu.sem.commonsystem.INodeProvider;
	uses dk.sdu.sem.gamesystem.assets.AssetDescriptor;
	uses dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader;
	uses dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
	uses dk.sdu.sem.gamesystem.services.IUpdate;
	uses dk.sdu.sem.gamesystem.services.ILateUpdate;
	uses dk.sdu.sem.gamesystem.services.IFixedUpdate;
	uses dk.sdu.sem.gamesystem.factories.IEntityFactory;
	uses dk.sdu.sem.gamesystem.rendering.IRenderSystem;
	uses dk.sdu.sem.player.IPlayerFactory;
	uses dk.sdu.sem.gamesystem.services.IStart;
	uses dk.sdu.sem.gamesystem.services.IGUIUpdate;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.gamesystem.data.SpriteNode,
		dk.sdu.sem.gamesystem.data.TilemapNode,
		dk.sdu.sem.gamesystem.data.AnimatorNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.gamesystem.data.SpriteNodeProvider,
		dk.sdu.sem.gamesystem.data.TilemapNodeProvider,
		dk.sdu.sem.gamesystem.data.AnimatorNodeProvider;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.gamesystem.animation.AnimationSystem;

	provides dk.sdu.sem.gamesystem.services.ILateUpdate with
		dk.sdu.sem.gamesystem.rendering.FXRenderSystem;

	provides dk.sdu.sem.gamesystem.rendering.IRenderSystem with
		dk.sdu.sem.gamesystem.rendering.FXRenderSystem;

	provides dk.sdu.sem.gamesystem.factories.IEntityFactory with
		dk.sdu.sem.gamesystem.factories.TilemapFactory;

	provides dk.sdu.sem.gamesystem.assets.loaders.IAssetLoader with
		dk.sdu.sem.gamesystem.assets.loaders.SpriteLoader,
		dk.sdu.sem.gamesystem.assets.loaders.SpriteAnimationLoader,
		dk.sdu.sem.gamesystem.assets.loaders.SpriteMapLoader,
		dk.sdu.sem.gamesystem.assets.loaders.ImageLoader;
}