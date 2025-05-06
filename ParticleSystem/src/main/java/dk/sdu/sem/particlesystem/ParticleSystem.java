package dk.sdu.sem.particlesystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Set;

public class ParticleSystem implements IUpdate, IGUIUpdate {
	private static final Logging LOGGER = Logging.createLogger("ParticleSystem", LoggingLevel.DEBUG);

	@Override
	public void update() {
		Set<ParticlesNode> particles = NodeManager.active().getNodes(ParticlesNode.class);
		particles.forEach(particle -> particle.emitter.update(particle));
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		Set<ParticlesNode> particles = NodeManager.active().getNodes(ParticlesNode.class);

		for (ParticlesNode node : particles) {
			LOGGER.debug("onGui(node=%s)", node);
			node.emitter.particles().forEachParticle(particle -> {
				LOGGER.debug("Rendering particle at %f %f", particle.position().x(), particle.position().y());

				gc.save();

				float scale = particle.scale();
				float rotation = particle.rotation();
				Vector2D position = particle.position();
				Color color = particle.color();

				gc.translate(position.x(), position.y());
				gc.rotate(rotation);
				gc.scale(scale, scale);
				gc.setFill(color);

				gc.fillRect(-2, -2, 4, 4);
				gc.restore();
			});
		}
	}
}
