package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameLoop {
	// Scheduler for fixed update loop (FixedUpdate)
	private final ScheduledExecutorService fixedUpdateScheduler;

	// Services loaded via ServiceLoader
	private final List<IFixedUpdate> fixedUpdateListeners = new ArrayList<>();
	private final List<IUpdate> updateListeners = new ArrayList<>();
	private final List<ILateUpdate> lateUpdateListeners = new ArrayList<>();
	private final List<IStart> startListeners = new ArrayList<>();

	public GameLoop() {
		// Load update listeners
		ServiceLoader.load(IFixedUpdate.class).forEach(fixedUpdateListeners::add);
		ServiceLoader.load(IUpdate.class).forEach(updateListeners::add);
		ServiceLoader.load(ILateUpdate.class).forEach(lateUpdateListeners::add);
		ServiceLoader.load(IStart.class).forEach(startListeners::add);

		fixedUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Starts the fixed update loop at 60Hz.
	 */
	public void start() {
		fixedUpdateScheduler.scheduleAtFixedRate(() -> {
			try {
				fixedUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 0, 16, TimeUnit.MILLISECONDS);

		startListeners.forEach(IStart::start);
	}

	/**
	 * FixedUpdate: Processes collisions, physics, and deterministic logic.
	 */
	private void fixedUpdate() {
		// Update the fixed timestep
		Time.fixedUpdate();

		// Get active scene
		Scene activeScene = SceneManager.getInstance().getActiveScene();

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