module Particles {
	exports dk.sdu.sem.particlesystem;
	requires CommonParticle;
	requires GameEngine;
	requires Common;
	requires javafx.graphics;

	provides dk.sdu.sem.commonsystem.Node with
		dk.sdu.sem.particlesystem.ParticlesNode;

	provides dk.sdu.sem.commonsystem.INodeProvider with
		dk.sdu.sem.particlesystem.ParticlesNode;

	provides dk.sdu.sem.gamesystem.services.IUpdate with
		dk.sdu.sem.particlesystem.ParticleSystem;

	provides dk.sdu.sem.gamesystem.services.IGUIUpdate with
		dk.sdu.sem.particlesystem.ParticleSystem;
}