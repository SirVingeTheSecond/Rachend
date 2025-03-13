package dk.sdu.sem.gamesystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonsystem.IEntity;
import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.data.Node;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameLoop {

	// Scheduler for fixed update loop (FixedUpdate).
	private final ScheduledExecutorService fixedUpdateScheduler;

	// Collision service loaded via JPMS ServiceLoader (implements ICollisionSPI).
	private final ICollisionSPI collisionService;

	// List of active entities (using the shared IEntity interface from Common).
	private final List<IEntity> entities = new ArrayList<>();

	private final HashMap<Class<? extends Node>, List<Node>> nodesDict = new HashMap<>();

	public GameLoop() {
		collisionService = ServiceLoader.load(ICollisionSPI.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No collision service found"));
		fixedUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Starts the fixed update loop at 60Hz.
	 */
	public void start() {
		fixedUpdateScheduler.scheduleAtFixedRate(this::fixedUpdate, 0, 16, TimeUnit.MILLISECONDS);

		getNodes().forEachRemaining(node -> {
			nodesDict.put(node.getClass(), new ArrayList<>());
		});
	}

	/**
	 * FixedUpdate: Processes collisions, physics, and deterministic entity logic.
	 */
	private void fixedUpdate() {
		// Update the fixed timestep simulation time.
		Time.fixedUpdate();

		// Process collisions.
		collisionService.processCollisions();

		getFixedUpdates().forEachRemaining(IFixedUpdate::fixedUpdate);
	}

	/**
	 * Update: Runs once per frame on the UI thread.
	 * @param dt Delta time (in seconds) since the last frame.
	 */
	public void update(double dt) {
		// Update the variable timestep simulation time.
		Time.update(dt);
		// Additional variable-rate logic (e.g., animations) can be processed here.
		getUpdates().forEachRemaining(IUpdate::update);
	}

	public void addEntity(Entity entity) {
		getNodes().forEachRemaining(node -> {
			if (node.matches(entity)) {
				nodesDict.get(node.getClass()).add(node);
			}
		});
	}

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

	/**
	 * Stops the fixed update loop.
	 */
	public void stop() {
		fixedUpdateScheduler.shutdown();
	}
}
