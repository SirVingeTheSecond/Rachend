import dk.sdu.sem.particlesystem.Particle;
import dk.sdu.sem.particlesystem.ParticleList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParticleList {
	@Test
	void particleListSize() {
		ParticleList pl = new ParticleList(10);
		assertTrue(pl.add(new Particle() {}));
		assertTrue(pl.add(new Particle() {}));

		assertEquals(2, pl.size());
	}
}
