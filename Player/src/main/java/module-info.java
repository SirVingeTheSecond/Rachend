module Player {
	requires Common;
	requires CommonPlayer;
	requires GameEngine;

	provides dk.sdu.sem.gamesystem.services.IUpdate
		with dk.sdu.sem.playersystem.PlayerSystem;
	provides dk.sdu.sem.commonsystem.Node
		with dk.sdu.sem.playersystem.PlayerNode;
}