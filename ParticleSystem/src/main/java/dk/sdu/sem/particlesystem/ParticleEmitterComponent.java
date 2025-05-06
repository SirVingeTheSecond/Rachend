package dk.sdu.sem.particlesystem;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.*;

record ParticleQueueEntry (Particle prototype, int amount) {}

public class ParticleEmitterComponent implements IComponent {
	private static final Logging LOGGER = Logging.createLogger("ParticleEmitterComponent", LoggingLevel.DEBUG);

	private static final int DEFAULT_MAX_PARTICLES = 100;

	private ParticleList particles;

	private Queue<ParticleQueueEntry> queue = new LinkedList<>();

	public ParticleEmitterComponent(int maxParticles) {
		this.particles = new ParticleList(maxParticles);
	}

	public void emit(Particle prototype, int amount) {
		if (amount == 0) { return; }
		this.queue.add(new ParticleQueueEntry(prototype, amount));
	}

	public ParticleList particles() {
		return particles;
	}

	public void update(ParticlesNode node) {
		LOGGER.debug("ParticleEmitterComponent::update(node=%s)", node);
//		particles.forEachParticle(particle -> particle.update((float)Time.getDeltaTime()));
		for (int i = 0; i < particles.length(); i++) {
			Particle particle = particles.get(i);
			if (particle == null) { continue; }

			particle.update((float)Time.getDeltaTime());
			if (particle.dead()) {
				particles.remove(i);
			}
		}

		ParticleQueueEntry entry;
		while ((entry = queue.poll()) != null) {
			LOGGER.debug("polled ParticleQueueEntry: %s", entry);

			for (int n = 0; n < entry.amount(); n++) {
				particles.add(entry.prototype());
			}
		}
	}
}
