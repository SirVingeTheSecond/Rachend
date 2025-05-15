package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.data.SpriteNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NodeManagerTest {
	private NodeManager nodeManager;
	private NodeFactory nodeFactory;

	private Entity entity;

	@BeforeEach
	void setUp() {
		nodeFactory = new NodeFactory();
		nodeManager = new NodeManager(nodeFactory);

		nodeManager.registerNodeType(SpriteNode.class, Set.of(TransformComponent.class, SpriteRendererComponent.class));

		entity = new Entity();
	}

	@Test
	void testEntityWithoutComponentsIsNotAddedToNodeCollection() {
		// Without required components
		nodeManager.processEntity(entity);

		// Entity should not be in the RenderNode collection
		assertTrue(nodeManager.getNodeEntities(SpriteNode.class).isEmpty());
	}

	@Test
	void testEntityWithRequiredComponentsIsAddedToNodeCollection() {
		// Create components
		IComponent TransformComponent = new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1));
		IComponent SpriteRendererComponent = new SpriteRendererComponent();

		// Required components
		entity.addComponent(TransformComponent);
		entity.addComponent(SpriteRendererComponent);

		// Process entity
		nodeManager.processEntity(entity);

		// Entity should be in the RenderNode collection
		Set<Entity> renderNodeEntities = nodeManager.getNodeEntities(SpriteNode.class);
		assertEquals(1, renderNodeEntities.size());
		assertTrue(renderNodeEntities.contains(entity));
	}

	@Test
	void testRemovingComponentRemovesEntityFromNodeCollection() {
		// Create components
		IComponent TransformComponent = new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1));
		IComponent SpriteRendererComponent = new SpriteRendererComponent();

		// Required components
		entity.addComponent(TransformComponent);
		entity.addComponent(SpriteRendererComponent);

		nodeManager.processEntity(entity);

		// Entity should be in the collection
		assertTrue(nodeManager.getNodeEntities(SpriteNode.class).contains(entity));

		// Remove a required component
		entity.removeComponent(SpriteRendererComponent.class);
		nodeManager.onComponentRemoved(entity, SpriteRendererComponent.class);

		// Entity should not be in the collection
		assertFalse(nodeManager.getNodeEntities(SpriteNode.class).contains(entity));
	}

	@Test
	void testCreateNodeForEntity() {
		// Create components
		IComponent TransformComponent = new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1));
		IComponent SpriteRendererComponent = new SpriteRendererComponent();

		// Add components
		entity.addComponent(TransformComponent);
		entity.addComponent(SpriteRendererComponent);

		// Create node for entity
		SpriteNode node = nodeManager.createNodeForEntity(SpriteNode.class, entity);

		// Node should be created as expected
		assertNotNull(node);
		assertTrue(node.getRequiredComponents().contains(TransformComponent.class));
		assertTrue(node.getRequiredComponents().contains(SpriteRendererComponent.class));
		assertEquals(entity, node.getEntity());
	}

	@Test
	void testCreateNodeReturnsNullForIncompatibleEntity() {
		// Add only one of the required components
		IComponent TransformComponent = new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1));

		entity.addComponent(TransformComponent);

		// Create node for entity
		SpriteNode node = nodeManager.createNodeForEntity(SpriteNode.class, entity);

		// Node should be null since entity doesn't have all required components
		assertNull(node);
	}

	@Test
	void testClear() {
		// Create components
		IComponent TransformComponent = new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1));
		IComponent SpriteRendererComponent = new SpriteRendererComponent();

		// Add components
		entity.addComponent(TransformComponent);
		entity.addComponent(SpriteRendererComponent);

		nodeManager.processEntity(entity);

		// Entity should be in the collection
		assertFalse(nodeManager.getNodeEntities(SpriteNode.class).isEmpty());

		// Clear all collections
		nodeManager.clear();

		// Collections should be empty
		assertTrue(nodeManager.getNodeEntities(SpriteNode.class).isEmpty());
	}

	@Test
	void testConcurrentAccessToGetNodes() throws InterruptedException {
		entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		entity.addComponent(new SpriteRendererComponent());
		nodeManager.processEntity(entity);

		int numThreads = 10;
		// CountDownLatch to synchronize the start of all threads
		CountDownLatch startLatch = new CountDownLatch(1);
		// CountDownLatch to wait for all threads to finish
		CountDownLatch finishLatch = new CountDownLatch(numThreads);

		// Create and start threads
		for (int i = 0; i < numThreads; i++) {
			Thread thread = new Thread(() -> {
				try {
					startLatch.await();

					for (int j = 0; j < 100; j++) {
						Set<SpriteNode> nodes = nodeManager.getNodes(SpriteNode.class);
						assertNotNull(nodes);
						assertEquals(1, nodes.size());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					finishLatch.countDown();
				}
			});
			thread.start();
		}

		// Signal all threads to start
		startLatch.countDown();

		// Wait for all threads to finish
		finishLatch.await(5, TimeUnit.SECONDS);

		// If we get here without exception, the test passes
	}

	@Test
	void testModificationDuringIteration() throws InterruptedException {
		entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		entity.addComponent(new SpriteRendererComponent());
		nodeManager.processEntity(entity);

		// CountDownLatch to ensure the iteration has started
		CountDownLatch iterationStarted = new CountDownLatch(1);
		// CountDownLatch to ensure the modification happens after iteration starts
		CountDownLatch readyForModification = new CountDownLatch(1);
		// CountDownLatch to wait for both threads to finish
		CountDownLatch finished = new CountDownLatch(2);

		// Thread for iteration
		Thread iterationThread = new Thread(() -> {
			try {
				Set<SpriteNode> nodes = nodeManager.getNodes(SpriteNode.class);

				iterationStarted.countDown();

				readyForModification.await();

				for (SpriteNode node : nodes) {
					assertNotNull(node);
					assertEquals(entity, node.getEntity());

					// Small delay to increase chance of concurrent modification
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				finished.countDown();
			}
		});

		// Thread for modification
		Thread modificationThread = new Thread(() -> {
			try {
				// Wait for iteration to start
				iterationStarted.await();

				// ready to modify
				readyForModification.countDown();

				Entity newEntity = new Entity();
				newEntity.addComponent(new TransformComponent(new Vector2D(10, 10), 0, new Vector2D(1, 1)));
				newEntity.addComponent(new SpriteRendererComponent());
				nodeManager.processEntity(newEntity);

				nodeManager.removeEntity(newEntity);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				finished.countDown();
			}
		});

		// Start both threads
		iterationThread.start();
		modificationThread.start();

		// Wait for both threads to finish
		finished.await(5, TimeUnit.SECONDS);

		// If we get here without exception, the test passes
	}

	@Test
	void testMultiThreadedEntityProcessingAndRemoval() throws InterruptedException {
		int numEntities = 100;
		int numThreads = 10;
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(numThreads);

		// Create threads that add and remove entities
		for (int i = 0; i < numThreads; i++) {
			final int threadId = i;
			Thread thread = new Thread(() -> {
				try {
					// Wait for start signal
					startLatch.await();

					// Each thread processes and removes entities
					for (int j = 0; j < numEntities / numThreads; j++) {
						Entity e = new Entity();
						e.addComponent(new TransformComponent(new Vector2D(j, threadId), 0, new Vector2D(1, 1)));
						e.addComponent(new SpriteRendererComponent());

						nodeManager.processEntity(e);

						// Get nodes while other threads are processing
						Set<SpriteNode> nodes = nodeManager.getNodes(SpriteNode.class);

						nodeManager.removeEntity(e);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					finishLatch.countDown();
				}
			});
			thread.start();
		}

		// Start all threads
		startLatch.countDown();

		// Wait for all threads to finish
		finishLatch.await(10, TimeUnit.SECONDS);

		// Check the final state - should be empty
		Set<SpriteNode> finalNodes = nodeManager.getNodes(SpriteNode.class);
		assertTrue(finalNodes.isEmpty());
	}
}