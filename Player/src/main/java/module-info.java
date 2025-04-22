module Player {
	requires GameEngine;
	requires CommonWeapon;
	requires CommonHealth;
	requires CommonInventory;
	requires CommonStats;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;

	uses dk.sdu.sem.commonweapon.IWeaponSPI;
	uses dk.sdu.sem.collision.IColliderFactory;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.playersystem.PlayerSystem,
		dk.sdu.sem.playersystem.PlayerAnimationController;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.playersystem.PlayerNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.playersystem.PlayerNodeProvider;

	provides dk.sdu.sem.player.IPlayerFactory with
		dk.sdu.sem.playersystem.PlayerFactory;

	provides dk.sdu.sem.gamesystem.assets.providers.IAssetProvider with
		dk.sdu.sem.playersystem.PlayerAssetProvider;

	exports dk.sdu.sem.playersystem;
}