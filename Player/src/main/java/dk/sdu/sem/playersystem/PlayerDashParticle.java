package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.particlesystem.Particle;
import javafx.scene.paint.Color;

public class PlayerDashParticle extends Particle {
	public PlayerDashParticle(Vector2D position) {
		this.spawn = position;
	}

	@Override
	public Vector2D position() {
		float t = lifetime/lifespan;
		return super.position()
			.add(Vector2D.UP.scale(t*10f))
			.add(Vector2D.RIGHT.scale((float) Math.sin(t * 12f)*5f));
	}

	@Override
	public Color color() {
		return Color.rgb(255, 255, 255, clamp(lifetime/lifespan, 0.0f, 1.0f));
	}

	@Override
	public float scale() {
		return remap(lifetime/lifespan, 0.0f, 1.0f, 1.5f, 0.2f);
	}
}
