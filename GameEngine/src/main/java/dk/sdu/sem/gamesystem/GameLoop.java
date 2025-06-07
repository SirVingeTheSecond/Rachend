package dk.sdu.sem.gamesystem;

import dk.sdu.sem.gamesystem.services.*;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
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
	private final List<IGUIUpdate> guiUpdateListeners = new ArrayList<>();
	private final List<IStart> startListeners = new ArrayList<>();

	public GameLoop() {
		// Load update listeners
		ServiceLoader.load(IFixedUpdate.class).forEach(fixedUpdateListeners::add);
		ServiceLoader.load(IUpdate.class).forEach(updateListeners::add);
		ServiceLoader.load(ILateUpdate.class).forEach(lateUpdateListeners::add);
		ServiceLoader.load(IGUIUpdate.class).forEach(guiUpdateListeners::add);
		ServiceLoader.load(IStart.class).forEach(startListeners::add);

		fixedUpdateScheduler = Executors.newScheduledThreadPool(1, r -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		});
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
		if (Time.getTimeScale() == 0)
			return;

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

		for (IUpdate listener : updateListeners) {
			listener.update();
		}
	}

	/**
	 * LateUpdate: Runs after Update
	 */
	public void lateUpdate() {
		for (ILateUpdate listener : lateUpdateListeners) {
			listener.lateUpdate();
		}
	}

	public void guiUpdate(GraphicsContext gc) {
		for (IGUIUpdate listener : guiUpdateListeners) {
			listener.onGUI(gc);
		}
	}

	/**
	 * Stops the fixed update loop.
	 */
	public void stop() {
		try {
			fixedUpdateScheduler.shutdown();
			if (!fixedUpdateScheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				fixedUpdateScheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			fixedUpdateScheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}