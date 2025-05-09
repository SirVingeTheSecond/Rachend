module DashAbility {
	requires Common;
	requires GameEngine;
	requires javafx.graphics;
    requires CommonCollision;

    exports dk.sdu.sem.dashability;

	provides dk.sdu.sem.gamesystem.services.IUpdate
		with dk.sdu.sem.dashability.DashAbilitySystem;

	provides dk.sdu.sem.commonsystem.INodeProvider
		with dk.sdu.sem.dashability.DashAbilityNode;

	provides dk.sdu.sem.commonsystem.Node
		with dk.sdu.sem.dashability.DashAbilityNode;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate
		with dk.sdu.sem.dashability.DashAbilityGUI;
}