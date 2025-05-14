package dk.sdu.sem.commonparticle;

import java.util.*;
import java.util.function.Consumer;

public class ParticleList implements List<Particle> {
	private Particle[] particles;
	private int size = 0;

	public ParticleList(int maxParticles) {
		assert maxParticles > 0;
		particles = new Particle[maxParticles];
		Arrays.fill(particles, null);
	}

	public int length() {
		return particles.length;
	}

	public void forEachParticle(Consumer<Particle> function) {
		for (int i = 0; i < particles.length; i++) {
			Particle particle = particles[i];
			if (particle == null) { continue; }
			function.accept(particle);
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public Iterator<Particle> iterator() {
		return new Iterator<Particle>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				for (int i = 0; i < particles.length; i++) {
					Particle particle = particles[i];
					if (particle == null) { continue; }
					return true;
				}
				return false;
			}

			@Override
			public Particle next() {
				return particles[i];
			}
		};
	}

	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}

	@Override
	public boolean add(Particle particle) {
		return find().map(index -> {
			size += 1;
			particles[index] = particle;
			return true;
		}).orElse(false);
	}

	private Optional<Integer> find() {
		for (int i = 0; i < particles.length; i++) {
			if (particles[i] == null) {
				return Optional.of(i);
			}
		}

		return Optional.empty();
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Particle> c) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Particle> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
		Arrays.fill(particles, null);
		size = 0;
	}

	@Override
	public Particle get(int index) {
		return particles[index];
	}

	@Override
	public Particle set(int index, Particle element) {
		return null;
	}

	@Override
	public void add(int index, Particle element) {

	}

	@Override
	public Particle remove(int index) {
		Particle particle = particles[index];
		particles[index] = null;
		size -= 1;
		return particle;
	}

	@Override
	public int indexOf(Object o) {
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		return 0;
	}

	@Override
	public ListIterator<Particle> listIterator() {
		return null;
	}

	@Override
	public ListIterator<Particle> listIterator(int index) {
		return null;
	}

	@Override
	public List<Particle> subList(int fromIndex, int toIndex) {
		return List.of();
	}
}

//
//public class ParticleList implements List<Particle> {
//	private Particle[] particles;
//	private int index;
//
//	// each time a particle is added or removed, the size variable is updated
//	// so it doesn't have to iterate over every element to figure out how many elements are here in total
//	private int size = 0;
//
//	public ParticleList(int maxParticles) {
//		this.particles = new Particle[maxParticles];
//		Arrays.fill(particles, null);
//
//		this.index = 0;
//	}
//
//	private boolean findAvailableIndex() {
//		if (size == particles.length) {
//			return false;
//		}
//
//		index = index % particles.length;
//		int original = index;
//
//		while (particles[index] != null) {
//			index = (index + 1) % particles.length;
//			if (original == index) {
//				return false;
//			}
//		}
//
//		return true;
//	}
//
//	@Override
//	public int size() {
//		return size;
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return size() == 0;
//	}
//
//	@Override
//	public boolean contains(Object o) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public Iterator<Particle> iterator() {
//		return new Iterator<Particle>() {
//			int i = 0;
//
//			@Override
//			public boolean hasNext() {
//				return i < size;
//			}
//
//			@Override
//			public Particle next() {
//
//				return particles[i++];
//			}
//		};
//	}
//
//	@Override
//	public Object[] toArray() {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public <T> T[] toArray(T[] a) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean add(Particle particle) {
//		if (findAvailableIndex()) {
//			particles[index] = particle;
//			size += 1;
//			return true;
//		}
//
//		return false;
//	}
//
//	@Override
//	public boolean remove(Object object) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean containsAll(Collection<?> c) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean addAll(Collection<? extends Particle> c) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean addAll(int index, Collection<? extends Particle> c) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean removeAll(Collection<?> c) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean retainAll(Collection<?> c) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void clear() {
//		Arrays.fill(particles, null);
//		this.size = 0;
//		this.index = 0;
//	}
//
//	@Override
//	public Particle get(int index) {
//		return particles[index];
//	}
//
//	@Override
//	public Particle set(int index, Particle element) {
//		Particle previous = particles[index];
//		particles[index] = element;
//
//		// update size based on presence change
//		if (previous != null && element == null) {
//			this.size -= 1;
//		}
//
//		if (previous == null && element != null) {
//			this.size += 1;
//		}
//
//		return previous;
//	}
//
//	@Override
//	public void add(int index, Particle element) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public Particle remove(int index) {
//		Particle particle = particles[index];
//		particles[index] = null;
//		this.size -= 1;
//		return particle;
//	}
//
//	@Override
//	public int indexOf(Object o) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public int lastIndexOf(Object o) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public ListIterator<Particle> listIterator() {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public ListIterator<Particle> listIterator(int index) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public List<Particle> subList(int fromIndex, int toIndex) {
//		throw new UnsupportedOperationException();
//	}
//}
