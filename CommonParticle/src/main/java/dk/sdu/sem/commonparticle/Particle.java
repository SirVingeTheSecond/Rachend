package dk.sdu.sem.commonparticle;

import dk.sdu.sem.commonsystem.Vector2D;
import javafx.scene.paint.Color;

public abstract class Particle {
	protected Vector2D spawn;
	protected float lifetime = 0.0f;
	protected float lifespan = 0.8f;

	public void update(float dt) {
		this.lifetime += dt;
	}

	public float scale() { return 1.0f; }
	public float rotation() { return 0.0f; }
	public Vector2D position() { return spawn; }
	public Color color() { return Color.GREEN; }

	// helper functions
	protected float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(v, max));
	}
	protected float remap(float v, float inMin, float inMax, float outMin, float outMax) {
		return (v - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
	}

	public boolean dead() {
		return lifetime >= lifespan;
	}
}
