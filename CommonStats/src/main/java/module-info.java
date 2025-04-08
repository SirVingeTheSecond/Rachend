module CommonStats {
	requires CommonItem;
	requires CommonPlayer;
	requires CommonEnemy;
	requires Common;
	requires GameEngine;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.commonstats.StatNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.commonstats.StatNode;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.commonstats.StatSystem;

	exports dk.sdu.sem.commonstats;
}