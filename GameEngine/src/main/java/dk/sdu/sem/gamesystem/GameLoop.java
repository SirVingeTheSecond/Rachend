package dk.sdu.sem.gamesystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.commonsystem.IEntity;
import dk.sdu.sem.gamesystem.services.IEntityPostProcessor;
import dk.sdu.sem.gamesystem.services.IEntityProcessor;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
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

	// Registered processors for per-entity updates.
	private final List<IEntityProcessor> processors = new ArrayList<>();

	// Registered post-processors.
	private final List<IEntityPostProcessor> postProcessors = new ArrayList<>();

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
	}

	/**
	 * FixedUpdate: Processes collisions, physics, and deterministic entity logic.
	 */
	private void fixedUpdate() {
		// Update the fixed timestep simulation time.
		Time.fixedUpdate();

		// Process collisions.
		collisionService.processCollisions();

		// Process each entity with registered IEntityProcessor instances.
		double fixedDeltaTime = Time.getFixedDeltaTime();
		for (IEntity entity : entities) {
			for (IEntityProcessor processor : processors) {
				processor.process(entity); // Add fixedDeltaTime as argument
			}
		}

		// Run post-processors afterward.
		for (IEntity entity : entities) {
			for (IEntityPostProcessor postProcessor : postProcessors) {
				postProcessor.postProcess(entity);
			}
		}
	}

	/**
	 * Update: Runs once per frame on the UI thread.
	 * @param dt Delta time (in seconds) since the last frame.
	 */
	public void update(double dt) {
		// Update the variable timestep simulation time.
		Time.update(dt);
		// Additional variable-rate logic (e.g., animations) can be processed here.
	}

	/**
	 * Renders the current game state onto the provided GraphicsContext.
	 */
	public void render(GraphicsContext gc) {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		// TODO: Render entities (e.g., using their TransformComponent data).
	}

	/**
	 * Adds an entity to the simulation.
	 * @param entity The entity (as an IEntity) to add.
	 */
	public void addEntity(IEntity entity) {
		entities.add(entity);
	}

	/**
	 * Registers an IEntityProcessor for updating entities.
	 * @param processor The processor to register.
	 */
	public void registerEntityProcessor(IEntityProcessor processor) {
		processors.add(processor);
	}

	/**
	 * Registers an IEntityPostProcessor for post-updating entities.
	 * @param postProcessor The post-processor to register.
	 */
	public void registerEntityPostProcessor(IEntityPostProcessor postProcessor) {
		postProcessors.add(postProcessor);
	}

	/**
	 * Stops the fixed update loop.
	 */
	public void stop() {
		fixedUpdateScheduler.shutdown();
	}
}
