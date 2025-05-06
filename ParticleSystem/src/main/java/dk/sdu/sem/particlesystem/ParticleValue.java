package dk.sdu.sem.particlesystem;

import dk.sdu.sem.commonsystem.Vector2D;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class ParticleValue<T> {
	private Supplier<T> supplier;

	private ParticleValue(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public static ParticleValue<Float> fixedFloat(float value) {
		return new ParticleValue<Float>(() -> value);
	}

	public static ParticleValue<Float> rangedFloat(float value, float epsilon) {
		return new ParticleValue<Float>(() -> value + (ThreadLocalRandom.current().nextFloat() * 2 * epsilon - epsilon));
	}

	public static ParticleValue<Float> fixedRangedFloat(float midpoint, float epsilon) {
		float value = midpoint + (ThreadLocalRandom.current().nextFloat() * 2 * epsilon - epsilon);
		return new ParticleValue<>(() -> value);
	}

	public static ParticleValue<Vector2D> fixedVector2D(Vector2D direction) {
		return new ParticleValue<Vector2D>(() -> direction);
	}

	public T value() {
		return supplier.get();
	}
}
