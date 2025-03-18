package dk.sdu.sem.gamesystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.gamesystem.data.Scene;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.gamesystem.systems.ISystem;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameLoop {
	// Scheduler for fixed update loop (FixedUpdate)
	private final ScheduledExecutorService fixedUpdateScheduler;

	// Collision service loaded via JPMS ServiceLoader
	private final ICollisionSPI collisionService;

	// Services loaded via ServiceLoader
	private final List<IFixedUpdate> fixedUpdateListeners = new ArrayList<>();
	private final List<IUpdate> updateListeners = new ArrayList<>();
	private final List<ILateUpdate> lateUpdateListeners = new ArrayList<>();
	private final List<ISystem<?>> systems = new ArrayList<>();

	public GameLoop() {
		// Load collision service
		collisionService = ServiceLoader.load(ICollisionSPI.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No collision service found"));

		// Load update listeners
		ServiceLoader.load(IFixedUpdate.class).forEach(fixedUpdateListeners::add);
		ServiceLoader.load(IUpdate.class).forEach(updateListeners::add);
		ServiceLoader.load(ILateUpdate.class).forEach(lateUpdateListeners::add);

		// Load systems
		ServiceLoader.load(ISystem.class).forEach(systems::add);

		fixedUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Starts the fixed update loop at 60Hz.
	 */
	public void start() {
		fixedUpdateScheduler.scheduleAtFixedRate(this::fixedUpdate, 0, 16, TimeUnit.MILLISECONDS);
	}

	/**
	 * FixedUpdate: Processes collisions, physics, and deterministic logic.
	 */
	private void fixedUpdate() {
		// Update the fixed timestep
		Time.fixedUpdate();

		// Process collisions
		collisionService.processCollisions();

		// Get active scene
		Scene activeScene = SceneManager.getInstance().getActiveScene();

		// Run in fixed update
		for (ISystem<?> system : systems) {
			if (system instanceof IFixedUpdate) {
				system.process(activeScene);
			}
		}

		// Call fixedUpdate on all listeners
		for (IFixedUpdate listener : fixedUpdateListeners) {
			listener.fixedUpdate();
		}
	}

	/**
	 * Update: Runs once per frame on the UI thread.
	 * @param dt Delta time (in seconds) since the last frame.
	 */
	public void update(double dt) {
		Time.update(dt);

		Scene activeScene = SceneManager.getInstance().getActiveScene();

		// Run in update
		for (ISystem<?> system : systems) {
			if (system instanceof IUpdate) {
				system.process(activeScene);
			}
		}

		// Call update on all listeners
		for (IUpdate listener : updateListeners) {
			listener.update();
		}
	}

	/**
	 * LateUpdate: Runs after Update
	 */
	public void lateUpdate() {
		Scene activeScene = SceneManager.getInstance().getActiveScene();

		// Run in late update
		for (ISystem<?> system : systems) {
			if (system instanceof ILateUpdate) {
				system.process(activeScene);
			}
		}

		// Call lateUpdate on all listeners
		for (ILateUpdate listener : lateUpdateListeners) {
			listener.lateUpdate();
		}
	}

	/*
	public void lateUpdate() {
		getLateUpdates().forEachRemaining(ILateUpdate::lateUpdate);
	}

	private static Iterator<? extends Node> getNodes() {
		return ServiceLoader.load(Node.class).iterator();
	}

	private static Iterator<? extends IFixedUpdate> getFixedUpdates() {
		return ServiceLoader.load(IFixedUpdate.class).iterator();
	}

	private static Iterator<? extends IUpdate> getUpdates() {
		return ServiceLoader.load(IUpdate.class).iterator();
	}

	private static Iterator<? extends ILateUpdate> getLateUpdates() {
		return ServiceLoader.load(ILateUpdate.class).iterator();
	}
	*/

	/*
	// How we could manually refresh ServiceLoader list
	// We would need a reference to the loader
	private static final ServiceLoader<IFixedUpdate> FIXED_UPDATE_LOADER = ServiceLoader.load(IFixedUpdate.class);

	private final List<IFixedUpdate> fixedUpdateListeners = new ArrayList<>();

	// We load as usual
	FIXED_UPDATE_LOADER.forEach(fixedUpdateListeners::add);

	public void refreshFixedUpdates() {
		// Clear the loader cache
		FIXED_UPDATE_LOADER.reload();

		// Clear and re‐add
		fixedUpdateListeners.clear();
		FIXED_UPDATE_LOADER.forEach(fixedUpdateListeners::add);
	}
	 */

	/**
	 * Stops the fixed update loop.
	 */
	public void stop() {
		fixedUpdateScheduler.shutdown();
	}
}

