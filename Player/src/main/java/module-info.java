module Player {
	requires Common;
	requires CommonPlayer;
	requires GameEngine;

	provides dk.sdu.sem.gamesystem.services.IUpdate
		with dk.sdu.sem.playersystem.PlayerSystem;
	provides dk.sdu.sem.commonsystem.Node
		with dk.sdu.sem.playersystem.PlayerNode;
	provides dk.sdu.sem.commonsystem.INodeProvider
		with dk.sdu.sem.playersystem.PlayerNodeProvider;

	exports dk.sdu.sem.playersystem;
}