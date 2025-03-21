module Player {
	requires Common;
	requires CommonPlayer;
	requires GameEngine;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.playersystem.PlayerSystem,
		dk.sdu.sem.playersystem.PlayerAnimationController;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.playersystem.PlayerNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.playersystem.PlayerNodeProvider;

	provides dk.sdu.sem.player.IPlayerFactory with
		dk.sdu.sem.playersystem.PlayerFactory;

	exports dk.sdu.sem.playersystem;
}