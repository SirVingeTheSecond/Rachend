import dk.sdu.sem.commonweaponsystem.IBulletWeapon;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;

module Player {
	requires Common;
	requires CommonPlayer;
	requires GameEngine;
	requires CommonCollision;
	requires CommonWeapon;
	uses IBulletWeapon;
	uses dk.sdu.sem.commonweaponsystem.IBulletWeaponSPI;
	requires CommonHealth;
	requires CommonInventory;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.playersystem.PlayerSystem,
		dk.sdu.sem.playersystem.PlayerAnimationController;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.playersystem.PlayerNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.playersystem.PlayerNodeProvider;

	provides dk.sdu.sem.player.IPlayerFactory with
		dk.sdu.sem.playersystem.PlayerFactory;

	provides IAssetProvider with
		dk.sdu.sem.playersystem.PlayerAssetProvider;

	exports dk.sdu.sem.playersystem;
}