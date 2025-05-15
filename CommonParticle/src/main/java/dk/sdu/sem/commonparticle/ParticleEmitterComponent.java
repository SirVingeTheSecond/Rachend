package dk.sdu.sem.commonparticle;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.*;

public class ParticleEmitterComponent implements IComponent {
	private static final Logging LOGGER = Logging.createLogger("ParticleEmitterComponent", LoggingLevel.DEBUG);

	private static final int DEFAULT_MAX_PARTICLES = 100;

	private ParticleList particles;

	public Queue<ParticleQueueEntry> getQueue() {
		return queue;
	}

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
}
