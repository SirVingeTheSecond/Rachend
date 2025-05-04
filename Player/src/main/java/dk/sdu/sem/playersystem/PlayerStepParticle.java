package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.particlesystem.Particle;

public class PlayerStepParticle extends Particle {
	public PlayerStepParticle(Vector2D position) {
		this.spawn = position;
	}

	@Override
	public Vector2D position() {
		float t = lifetime/lifespan;
		return super.position()
			.add(Vector2D.UP.scale(t*10f))
			.add(Vector2D.RIGHT.scale((float) Math.sin(t * 12f)));
	}

	@Override
	public float scale() {
		return clamp(1f - lifetime/lifespan, 0f, 1f);
	}
}
