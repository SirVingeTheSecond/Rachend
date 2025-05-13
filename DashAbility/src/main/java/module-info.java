module DashAbility {
	requires CommonParticle;
	requires GameEngine;
	requires CommonPlayer;
	requires CommonCollision;
	requires Common;
	requires javafx.graphics;

	exports dk.sdu.sem.dashability;

	provides dk.sdu.sem.gamesystem.services.IUpdate
		with dk.sdu.sem.dashability.DashAbilitySystem;

	provides dk.sdu.sem.commonsystem.INodeProvider
		with dk.sdu.sem.dashability.DashAbilityNode;

	provides dk.sdu.sem.commonsystem.Node
		with dk.sdu.sem.dashability.DashAbilityNode;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate
		with dk.sdu.sem.dashability.DashAbilityGUI;

	provides dk.sdu.sem.commonsystem.IEntityLifecycleListener
		with dk.sdu.sem.dashability.DashAbilityLifetime;
}