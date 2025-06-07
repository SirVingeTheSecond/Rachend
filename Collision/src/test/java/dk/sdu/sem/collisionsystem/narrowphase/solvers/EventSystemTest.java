package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import static org.junit.jupiter.api.Assertions.*;

import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.ITriggerListener;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import dk.sdu.sem.collision.events.*;
import dk.sdu.sem.collision.components.*;
import dk.sdu.sem.collision.data.*;
import dk.sdu.sem.collisionsystem.events.EventSystem;
import dk.sdu.sem.collisionsystem.systems.CollisionListenerSystem;
import dk.sdu.sem.commonsystem.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive test suite for the EventSystem using JUnit 5
 */
@DisplayName("EventSystem Test Suite")
public class EventSystemTest {
	private Scene scene;
	private EventSystem eventSystem;
	private CollisionListenerSystem listenerSystem;

	@BeforeAll
	static void initJavaFX() {
		if (!Platform.isFxApplicationThread()) {
			CountDownLatch latch = new CountDownLatch(1);
			Platform.startup(latch::countDown);
			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@BeforeEach
	void setup() {
		scene = new Scene("TestScene");
		Scene.setActiveScene(scene);
		eventSystem = EventSystem.getInstance();
		eventSystem.clear(); // Start with a clean event system
		listenerSystem = new CollisionListenerSystem();
		listenerSystem.start();
	}

	@AfterEach
	void cleanup() {
		eventSystem.clear();
		scene.clear();
	}

	/**
	 * Basic listener component that implements ICollisionListener and ITriggerListener
	 */
	private static class TestListener implements IComponent, ICollisionListener, ITriggerListener {
		// Collision event counters
		public AtomicInteger collisionEnterCount = new AtomicInteger(0);
		public AtomicInteger collisionStayCount = new AtomicInteger(0);
		public AtomicInteger collisionExitCount = new AtomicInteger(0);

		// Trigger event counters
		public AtomicInteger triggerEnterCount = new AtomicInteger(0);
		public AtomicInteger triggerStayCount = new AtomicInteger(0);
		public AtomicInteger triggerExitCount = new AtomicInteger(0);

		// Event tracking
		public List<Entity> collidedEntities = new ArrayList<>();
		public List<Entity> triggeredEntities = new ArrayList<>();

		// Last received contact point
		public ContactPoint lastContact;

		@Override
		public void onCollisionEnter(CollisionEnterEvent event) {
			collisionEnterCount.incrementAndGet();
			collidedEntities.add(event.getOther());
			lastContact = event.getContact();
		}

		@Override
		public void onCollisionStay(CollisionStayEvent event) {
			collisionStayCount.incrementAndGet();
			lastContact = event.getContact();
		}

		@Override
		public void onCollisionExit(CollisionExitEvent event) {
			collisionExitCount.incrementAndGet();
			lastContact = event.getContact();
		}

		@Override
		public void onTriggerEnter(TriggerEnterEvent event) {
			triggerEnterCount.incrementAndGet();
			triggeredEntities.add(event.getOther());
		}

		@Override
		public void onTriggerStay(TriggerStayEvent event) {
			triggerStayCount.incrementAndGet();
		}

		@Override
		public void onTriggerExit(TriggerExitEvent event) {
			triggerExitCount.incrementAndGet();
		}
	}

	/**
	 * Test event type for generic functionality testing
	 */
	private static class TestEvent {
		private final String message;

		public TestEvent(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	@Nested
	@DisplayName("Basic EventSystem Functionality")
	class BasicEventSystemFunctionality {

		@Test
		@DisplayName("Should handle basic publish and subscribe operations")
		void testBasicPublishSubscribe() throws InterruptedException {
			final AtomicInteger callCount = new AtomicInteger(0);
			final String testMessage = "Hello, EventSystem!";
			final CountDownLatch latch = new CountDownLatch(1);

			// Create and subscribe a listener with synchronization
			IEventListener<TestEvent> listener = event -> {
				callCount.incrementAndGet();
				assertEquals(testMessage, event.getMessage());
				latch.countDown(); // Signal completion
			};

			eventSystem.subscribe(TestEvent.class, listener);

			// Publish an event
			eventSystem.publish(new TestEvent(testMessage));

			// Wait for event processing to complete (max 5 seconds)
			assertTrue(latch.await(5, TimeUnit.SECONDS),
				"Event handler should complete within timeout");

			// Verify listener was called once
			assertEquals(1, callCount.get());

			// Test second publication with new latch
			final CountDownLatch latch2 = new CountDownLatch(1);
			final AtomicInteger callCount2 = new AtomicInteger(callCount.get());

			IEventListener<TestEvent> listener2 = event -> {
				callCount2.incrementAndGet();
				latch2.countDown();
			};

			eventSystem.unsubscribe(TestEvent.class, listener);
			eventSystem.subscribe(TestEvent.class, listener2);

			eventSystem.publish(new TestEvent(testMessage));
			assertTrue(latch2.await(5, TimeUnit.SECONDS));
			assertEquals(2, callCount2.get());
		}

		@Test
		@DisplayName("Should properly unsubscribe listeners")
		void testUnsubscribe() throws InterruptedException {
			final AtomicInteger callCount = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(1);

			// Create and subscribe a listener
			IEventListener<TestEvent> listener = event -> {
				callCount.incrementAndGet();
				latch.countDown();
			};

			eventSystem.subscribe(TestEvent.class, listener);

			// Publish an event
			eventSystem.publish(new TestEvent("Test"));
			assertTrue(latch.await(5, TimeUnit.SECONDS));
			assertEquals(1, callCount.get());

			// Unsubscribe and publish again
			eventSystem.unsubscribe(TestEvent.class, listener);
			eventSystem.publish(new TestEvent("Test"));

			// Wait a brief moment to ensure no event is processed
			Thread.sleep(100);

			// Verify listener was not called again
			assertEquals(1, callCount.get());
		}

		@Test
		@DisplayName("Should support multiple listeners for the same event type")
		void testMultipleListeners() throws InterruptedException {
			final AtomicInteger count1 = new AtomicInteger(0);
			final AtomicInteger count2 = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(2); // Wait for both listeners

			// Create and subscribe two listeners
			IEventListener<TestEvent> listener1 = event -> {
				count1.incrementAndGet();
				latch.countDown();
			};

			IEventListener<TestEvent> listener2 = event -> {
				count2.incrementAndGet();
				latch.countDown();
			};

			eventSystem.subscribe(TestEvent.class, listener1);
			eventSystem.subscribe(TestEvent.class, listener2);

			// Publish an event
			eventSystem.publish(new TestEvent("Test"));

			// Wait for both listeners to complete
			assertTrue(latch.await(5, TimeUnit.SECONDS));

			// Verify both listeners were called
			assertEquals(1, count1.get());
			assertEquals(1, count2.get());

			// Test unsubscribing one listener
			final CountDownLatch latch2 = new CountDownLatch(1);
			final AtomicInteger count1_after = new AtomicInteger(count1.get());
			final AtomicInteger count2_after = new AtomicInteger(count2.get());

			eventSystem.unsubscribe(TestEvent.class, listener1);

			// Subscribe new listener2 that updates counter and signals latch
			eventSystem.unsubscribe(TestEvent.class, listener2);
			IEventListener<TestEvent> newListener2 = event -> {
				count2_after.incrementAndGet();
				latch2.countDown();
			};
			eventSystem.subscribe(TestEvent.class, newListener2);

			eventSystem.publish(new TestEvent("Test"));
			assertTrue(latch2.await(5, TimeUnit.SECONDS));

			// Verify only listener2 was called
			assertEquals(1, count1_after.get()); // Should remain unchanged
			assertEquals(2, count2_after.get()); // Should increment
		}

		@Test
		@DisplayName("Should clear all listeners when clear() is called")
		void testClearRemovesAllListeners() throws InterruptedException {
			final AtomicInteger count1 = new AtomicInteger(0);
			final AtomicInteger count2 = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(2);

			// Subscribe to different event types
			IEventListener<TestEvent> listener1 = event -> {
				count1.incrementAndGet();
				latch.countDown();
			};

			IEventListener<CollisionEnterEvent> listener2 = event -> {
				count2.incrementAndGet();
				latch.countDown();
			};

			eventSystem.subscribe(TestEvent.class, listener1);
			eventSystem.subscribe(CollisionEnterEvent.class, listener2);

			// Publish events
			eventSystem.publish(new TestEvent("Test"));
			Entity entity = new Entity();
			Entity other = new Entity();
			eventSystem.publish(new CollisionEnterEvent(entity, other, null));

			assertTrue(latch.await(5, TimeUnit.SECONDS));
			assertEquals(1, count1.get());
			assertEquals(1, count2.get());

			// Clear all listeners
			eventSystem.clear();

			// Publish events again
			eventSystem.publish(new TestEvent("Test"));
			eventSystem.publish(new CollisionEnterEvent(entity, other, null));

			// Wait briefly to ensure no events are processed
			Thread.sleep(100);

			// Verify no additional callbacks were made
			assertEquals(1, count1.get());
			assertEquals(1, count2.get());
		}

		@Test
		@DisplayName("Should handle publishing events with no subscribers gracefully")
		void testPublishNoSubscribers() {
			// This shouldn't throw an exception
			assertDoesNotThrow(() -> eventSystem.publish(new TestEvent("No subscribers")));

			// Now add a subscriber to a different event type
			final AtomicInteger count = new AtomicInteger(0);
			eventSystem.subscribe(CollisionEnterEvent.class, event -> count.incrementAndGet());

			// Publish event with no subscribers
			assertDoesNotThrow(() -> eventSystem.publish(new TestEvent("Still no subscribers")));

			// Verify the other subscriber wasn't called
			assertEquals(0, count.get());
		}
	}

	@Nested
	@DisplayName("Collision Event Tests")
	class CollisionEventTests {

		@Test
		@DisplayName("Should handle collision enter events correctly")
		void testCollisionEnterEvent() {
			// Create an entity with collision components
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create a target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create a contact point
			Vector2D contactPoint = new Vector2D(5, 5);
			Vector2D normal = new Vector2D(1, 0);
			float separation = -2.5f;
			ContactPoint contact = new ContactPoint(contactPoint, normal, separation);

			// Create and publish a collision enter event
			CollisionEnterEvent enterEvent = new CollisionEnterEvent(entity, otherEntity, contact);
			eventSystem.publish(enterEvent);

			// Verify the listener was called
			assertEquals(1, listener.collisionEnterCount.get());
			assertTrue(listener.collidedEntities.contains(otherEntity));

			// Verify contact information was passed
			assertNotNull(listener.lastContact);
			assertEquals(contactPoint, listener.lastContact.getPoint());
			assertEquals(normal, listener.lastContact.getNormal());
			assertEquals(separation, listener.lastContact.getSeparation(), 0.001f);
		}

		@Test
		@DisplayName("Should handle collision stay events correctly")
		void testCollisionStayEvent() {
			// Create an entity with collision components
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create a target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create a contact point
			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), -2.5f);

			// Create and publish a collision stay event
			CollisionStayEvent stayEvent = new CollisionStayEvent(entity, otherEntity, contact);

			// Publish multiple times to simulate continuous collision
			for (int i = 0; i < 5; i++) {
				eventSystem.publish(stayEvent);
			}

			// Verify the listener was called the correct number of times
			assertEquals(5, listener.collisionStayCount.get());

			// Verify contact information was passed in the last call
			assertNotNull(listener.lastContact);
			assertEquals(contact.getPoint(), listener.lastContact.getPoint());
		}

		@Test
		@DisplayName("Should handle collision exit events correctly")
		void testCollisionExitEvent() {
			// Create an entity with collision components
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create a target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create a contact point
			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			// Create and publish a collision exit event
			CollisionExitEvent exitEvent = new CollisionExitEvent(entity, otherEntity, contact);
			eventSystem.publish(exitEvent);

			// Verify the listener was called
			assertEquals(1, listener.collisionExitCount.get());
			assertNotNull(listener.lastContact);
		}

		@Test
		@DisplayName("Should handle contact normal direction correctly")
		void testContactNormalDirection() {
			// Create two entities
			Entity entityA = new Entity();
			entityA.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listenerA = new TestListener();
			entityA.addComponent(listenerA);
			entityA.addComponent(new BoxColliderComponent(entityA, 10, 10));
			entityA.addComponent(new CollisionStateComponent());

			Entity entityB = new Entity();
			entityB.addComponent(new TransformComponent(new Vector2D(15, 0), 0));
			TestListener listenerB = new TestListener();
			entityB.addComponent(listenerB);
			entityB.addComponent(new BoxColliderComponent(entityB, 10, 10));
			entityB.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entityA);
			scene.addEntity(entityB);
			listenerSystem.update();

			// Create a contact point with normal pointing from A to B
			Vector2D normal = new Vector2D(1, 0); // Points right
			ContactPoint contact = new ContactPoint(new Vector2D(10, 5), normal, -5f);

			// Create events for both entities
			CollisionEnterEvent eventA = new CollisionEnterEvent(entityA, entityB, contact);

			// Create reversed contact for B
			ContactPoint reversedContact = new ContactPoint(
				contact.getPoint(),
				normal.scale(-1), // Normal should be flipped
				contact.getSeparation()
			);
			CollisionEnterEvent eventB = new CollisionEnterEvent(entityB, entityA, reversedContact);

			// Publish both events
			eventSystem.publish(eventA);
			eventSystem.publish(eventB);

			// Verify both listeners received their events
			assertEquals(1, listenerA.collisionEnterCount.get());
			assertEquals(1, listenerB.collisionEnterCount.get());

			// Verify normals point in the expected directions
			assertEquals(normal, listenerA.lastContact.getNormal());
			assertEquals(normal.scale(-1), listenerB.lastContact.getNormal());
		}

		@Test
		@DisplayName("Should stop sending collision events when entity is removed")
		void testEntityRemovalStopsCollisionEvents() {
			// Create an entity with collision components
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create a target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create a contact point
			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			// Create collision events
			CollisionEnterEvent enterEvent = new CollisionEnterEvent(entity, otherEntity, contact);
			CollisionStayEvent stayEvent = new CollisionStayEvent(entity, otherEntity, contact);
			CollisionExitEvent exitEvent = new CollisionExitEvent(entity, otherEntity, contact);

			// Publish enter event
			eventSystem.publish(enterEvent);
			assertEquals(1, listener.collisionEnterCount.get());

			// Remove entity from scene
			scene.removeEntity(entity);
			listenerSystem.update();

			// Reset counters
			listener.collisionEnterCount.set(0);
			listener.collisionStayCount.set(0);
			listener.collisionExitCount.set(0);

			// Publish all events again
			eventSystem.publish(enterEvent);
			eventSystem.publish(stayEvent);
			eventSystem.publish(exitEvent);

			// Verify no events were delivered
			assertEquals(0, listener.collisionEnterCount.get());
			assertEquals(0, listener.collisionStayCount.get());
			assertEquals(0, listener.collisionExitCount.get());
		}
	}

	@Nested
	@DisplayName("Trigger Event Tests")
	class TriggerEventTests {

		@Test
		@DisplayName("Should handle trigger enter events correctly")
		void testTriggerEnterEvent() {
			// Create an entity with trigger component
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);

			// Create a trigger collider
			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create and publish trigger enter event
			TriggerEnterEvent enterEvent = new TriggerEnterEvent(entity, otherEntity);
			eventSystem.publish(enterEvent);

			// Verify the listener was called
			assertEquals(1, listener.triggerEnterCount.get());
			assertTrue(listener.triggeredEntities.contains(otherEntity));
		}

		@Test
		@DisplayName("Should handle trigger stay events correctly")
		void testTriggerStayEvent() {
			// Create an entity with trigger component
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);

			// Create a trigger collider
			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create and publish multiple trigger stay events
			TriggerStayEvent stayEvent = new TriggerStayEvent(entity, otherEntity);

			for (int i = 0; i < 5; i++) {
				eventSystem.publish(stayEvent);
			}

			// Verify the listener was called the correct number of times
			assertEquals(5, listener.triggerStayCount.get());
		}

		@Test
		@DisplayName("Should handle trigger exit events correctly")
		void testTriggerExitEvent() {
			// Create an entity with trigger component
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);

			// Create a trigger collider
			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create and publish trigger exit event
			TriggerExitEvent exitEvent = new TriggerExitEvent(entity, otherEntity);
			eventSystem.publish(exitEvent);

			// Verify the listener was called
			assertEquals(1, listener.triggerExitCount.get());
		}

		@Test
		@DisplayName("Should stop sending trigger events when entity is removed")
		void testEntityRemovalStopsTriggerEvents() {
			// Create an entity with trigger component
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);

			// Create a trigger collider
			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create target entity
			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			// Create trigger events
			TriggerEnterEvent enterEvent = new TriggerEnterEvent(entity, otherEntity);
			TriggerStayEvent stayEvent = new TriggerStayEvent(entity, otherEntity);
			TriggerExitEvent exitEvent = new TriggerExitEvent(entity, otherEntity);

			// Publish enter event
			eventSystem.publish(enterEvent);
			assertEquals(1, listener.triggerEnterCount.get());

			// Remove entity from scene
			scene.removeEntity(entity);
			listenerSystem.update();

			// Reset counters
			listener.triggerEnterCount.set(0);
			listener.triggerStayCount.set(0);
			listener.triggerExitCount.set(0);

			// Publish all events again
			eventSystem.publish(enterEvent);
			eventSystem.publish(stayEvent);
			eventSystem.publish(exitEvent);

			// Verify no events were delivered
			assertEquals(0, listener.triggerEnterCount.get());
			assertEquals(0, listener.triggerStayCount.get());
			assertEquals(0, listener.triggerExitCount.get());
		}
	}

	@Nested
	@DisplayName("Entity Lifecycle Tests")
	class EntityLifecycleTests {

		@Test
		@DisplayName("Should handle multiple entities with listeners correctly")
		void testMultipleEntitiesWithListeners() {
			int entityCount = 10;
			List<Entity> entities = new ArrayList<>();
			List<TestListener> listeners = new ArrayList<>();

			// Create multiple entities with collision listeners
			for (int i = 0; i < entityCount; i++) {
				Entity entity = new Entity();
				entity.addComponent(new TransformComponent(new Vector2D(i * 10, 0), 0));
				TestListener listener = new TestListener();
				entity.addComponent(listener);
				entity.addComponent(new BoxColliderComponent(entity, 10, 10));
				entity.addComponent(new CollisionStateComponent());

				scene.addEntity(entity);
				entities.add(entity);
				listeners.add(listener);
			}

			// Process all entities
			listenerSystem.update();

			// Create a common target entity
			Entity targetEntity = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			// Publish events to all entities
			for (int i = 0; i < entityCount; i++) {
				CollisionEnterEvent event = new CollisionEnterEvent(entities.get(i), targetEntity, contact);
				eventSystem.publish(event);
			}

			// Verify all listeners received their events
			for (int i = 0; i < entityCount; i++) {
				assertEquals(1, listeners.get(i).collisionEnterCount.get());
			}

			// Remove half the entities
			for (int i = 0; i < entityCount / 2; i++) {
				scene.removeEntity(entities.get(i));
			}

			// Process entity removal
			listenerSystem.update();

			// Reset all counters
			for (TestListener listener : listeners) {
				listener.collisionEnterCount.set(0);
			}

			// Publish events again
			for (int i = 0; i < entityCount; i++) {
				CollisionEnterEvent event = new CollisionEnterEvent(entities.get(i), targetEntity, contact);
				eventSystem.publish(event);
			}

			// Verify only remaining entities received events
			for (int i = 0; i < entityCount / 2; i++) {
				assertEquals(0, listeners.get(i).collisionEnterCount.get(),
					"Removed entity should not receive events");
			}

			for (int i = entityCount / 2; i < entityCount; i++) {
				assertEquals(1, listeners.get(i).collisionEnterCount.get(),
					"Remaining entity should receive events");
			}
		}

		@Test
		@DisplayName("Should properly isolate events when switching scenes")
		void testSceneSwitchingIsolation() {
			// Create two scenes
			Scene sceneA = new Scene("SceneA");
			Scene sceneB = new Scene("SceneB");

			// Create entities in both scenes
			Entity entityA = new Entity();
			entityA.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listenerA = new TestListener();
			entityA.addComponent(listenerA);
			entityA.addComponent(new BoxColliderComponent(entityA, 10, 10));
			entityA.addComponent(new CollisionStateComponent());

			Entity entityB = new Entity();
			entityB.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listenerB = new TestListener();
			entityB.addComponent(listenerB);
			entityB.addComponent(new BoxColliderComponent(entityB, 10, 10));
			entityB.addComponent(new CollisionStateComponent());

			// Add entities to their scenes
			Scene.setActiveScene(sceneA);
			sceneA.addEntity(entityA);
			listenerSystem.update();

			Scene.setActiveScene(sceneB);
			sceneB.addEntity(entityB);
			listenerSystem.update();

			// Create common test entities
			Entity targetA = new Entity();
			Entity targetB = new Entity();
			ContactPoint contactPoint = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			// Create events for both collision and trigger types
			CollisionEnterEvent collisionEventA = new CollisionEnterEvent(entityA, targetA, contactPoint);
			TriggerEnterEvent triggerEventA = new TriggerEnterEvent(entityA, targetA);

			CollisionEnterEvent collisionEventB = new CollisionEnterEvent(entityB, targetB, contactPoint);
			TriggerEnterEvent triggerEventB = new TriggerEnterEvent(entityB, targetB);

			// Test with scene A active
			Scene.setActiveScene(sceneA);

			// Publish all events
			eventSystem.publish(collisionEventA);
			eventSystem.publish(triggerEventA);
			eventSystem.publish(collisionEventB);
			eventSystem.publish(triggerEventB);

			// Verify only entity A received its events
			assertEquals(1, listenerA.collisionEnterCount.get());
			assertEquals(1, listenerA.triggerEnterCount.get());
			assertEquals(0, listenerB.collisionEnterCount.get());
			assertEquals(0, listenerB.triggerEnterCount.get());

			// Reset counters
			listenerA.collisionEnterCount.set(0);
			listenerA.triggerEnterCount.set(0);

			// Test with scene B active
			Scene.setActiveScene(sceneB);

			// Publish all events again
			eventSystem.publish(collisionEventA);
			eventSystem.publish(triggerEventA);
			eventSystem.publish(collisionEventB);
			eventSystem.publish(triggerEventB);

			// Verify only entity B received its events
			assertEquals(0, listenerA.collisionEnterCount.get());
			assertEquals(0, listenerA.triggerEnterCount.get());
			assertEquals(1, listenerB.collisionEnterCount.get());
			assertEquals(1, listenerB.triggerEnterCount.get());
		}

		@Test
		@DisplayName("Should handle entities moved between scenes correctly")
		void testEntityMovedBetweenScenes() {
			// Create two scenes
			Scene sceneA = new Scene("SceneA");
			Scene sceneB = new Scene("SceneB");

			// Create a movable entity
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			// Add to scene A first
			Scene.setActiveScene(sceneA);
			sceneA.addEntity(entity);
			listenerSystem.update();

			// Create events
			Entity target = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			CollisionEnterEvent collisionEvent = new CollisionEnterEvent(entity, target, contact);
			TriggerEnterEvent triggerEvent = new TriggerEnterEvent(entity, target);

			// Publish events in scene A
			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			// Verify events were received
			assertEquals(1, listener.collisionEnterCount.get());
			assertEquals(1, listener.triggerEnterCount.get());

			// Move entity to scene B
			sceneA.removeEntity(entity);
			Scene.setActiveScene(sceneB);
			sceneB.addEntity(entity);
			listenerSystem.update();

			// Reset counters
			listener.collisionEnterCount.set(0);
			listener.triggerEnterCount.set(0);

			// Set scene A active and publish events
			Scene.setActiveScene(sceneA);
			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			// Entity should not receive events when active scene is A
			assertEquals(0, listener.collisionEnterCount.get());
			assertEquals(0, listener.triggerEnterCount.get());

			// Set scene B active and publish events
			Scene.setActiveScene(sceneB);
			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			// Entity should receive events when active scene is B
			assertEquals(1, listener.collisionEnterCount.get());
			assertEquals(1, listener.triggerEnterCount.get());
		}
	}

	@Nested
	@DisplayName("Edge Case Tests")
	class EdgeCaseTests {

		@Test
		@DisplayName("Should handle rapid entity creation and destruction")
		void testRapidEntityCreationDestruction() {
			// Create a large number of entities and immediately destroy some
			int totalEntities = 50;
			List<Entity> entities = new ArrayList<>();
			List<TestListener> listeners = new ArrayList<>();

			for (int i = 0; i < totalEntities; i++) {
				Entity entity = new Entity();
				entity.addComponent(new TransformComponent(new Vector2D(i, 0), 0));
				TestListener listener = new TestListener();
				entity.addComponent(listener);
				entity.addComponent(new BoxColliderComponent(entity, 10, 10));
				entity.addComponent(new CollisionStateComponent());

				scene.addEntity(entity);
				entities.add(entity);
				listeners.add(listener);

				// Immediately remove even-numbered entities
				if (i % 2 == 0) {
					scene.removeEntity(entity);
				}
			}

			// Process all entities
			listenerSystem.update();

			// Create and publish events to all entities
			Entity target = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			for (int i = 0; i < totalEntities; i++) {
				CollisionEnterEvent event = new CollisionEnterEvent(entities.get(i), target, contact);
				eventSystem.publish(event);
			}

			// Verify only odd-indexed entities received events
			for (int i = 0; i < totalEntities; i++) {
				if (i % 2 == 0) {
					assertEquals(0, listeners.get(i).collisionEnterCount.get(),
						"Removed entity should not receive events");
				} else {
					assertEquals(1, listeners.get(i).collisionEnterCount.get(),
						"Active entity should receive events");
				}
			}
		}

		@Test
		@DisplayName("Should handle component removal correctly")
		void testComponentRemoval() {
			// Create an entity with collision and listener components
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			// Add to scene and process
			scene.addEntity(entity);
			listenerSystem.update();

			// Create events
			Entity target = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			CollisionEnterEvent collisionEvent = new CollisionEnterEvent(entity, target, contact);
			TriggerEnterEvent triggerEvent = new TriggerEnterEvent(entity, target);

			// Publish events
			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			// Verify events were received
			assertEquals(1, listener.collisionEnterCount.get());
			assertEquals(1, listener.triggerEnterCount.get());

			// Remove listener component
			entity.removeComponent(TestListener.class);

			// Reset counters (technically not needed since the instance is removed)
			listener.collisionEnterCount.set(0);
			listener.triggerEnterCount.set(0);

			// Update listener system
			listenerSystem.update();

			// Publish events again
			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			// Verify no events were received after component removal
			assertEquals(0, listener.collisionEnterCount.get());
			assertEquals(0, listener.triggerEnterCount.get());
		}

		@Test
		@DisplayName("Should handle concurrent event processing correctly")
		void testConcurrentEventProcessing() throws InterruptedException {
			// Create entities with listeners
			int entityCount = 10;
			List<Entity> entities = new ArrayList<>();
			List<TestListener> listeners = new ArrayList<>();

			for (int i = 0; i < entityCount; i++) {
				Entity entity = new Entity();
				entity.addComponent(new TransformComponent(new Vector2D(i * 10, 0), 0));
				TestListener listener = new TestListener();
				entity.addComponent(listener);
				entity.addComponent(new BoxColliderComponent(entity, 10, 10));
				entity.addComponent(new CollisionStateComponent());

				scene.addEntity(entity);
				entities.add(entity);
				listeners.add(listener);
			}

			// Process all entities
			listenerSystem.update();

			// Create a target entity and contact
			Entity target = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			// Create events for each entity
			List<CollisionEnterEvent> events = new ArrayList<>();
			for (Entity entity : entities) {
				events.add(new CollisionEnterEvent(entity, target, contact));
			}

			// Use a countdown latch to synchronize threads
			final CountDownLatch startLatch = new CountDownLatch(1);
			final CountDownLatch finishLatch = new CountDownLatch(entityCount);

			// Start multiple threads to publish events concurrently
			for (int i = 0; i < entityCount; i++) {
				final int index = i;
				new Thread(() -> {
					try {
						startLatch.await(); // Wait for the signal to start

						// Publish an event
						eventSystem.publish(events.get(index));

						finishLatch.countDown(); // Signal that this thread is done
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}).start();
			}

			// Start all threads simultaneously
			startLatch.countDown();

			// Wait for all threads to finish (with timeout)
			assertTrue(finishLatch.await(5, TimeUnit.SECONDS),
				"All threads should complete within the timeout");

			// Verify all events were received exactly once
			for (int i = 0; i < entityCount; i++) {
				assertEquals(1, listeners.get(i).collisionEnterCount.get(),
					"Each entity should receive exactly one event");
			}
		}

		@Test
		@DisplayName("Should handle exceptions in listeners gracefully")
		void testExceptionInListenerDoesNotAffectOtherListeners() {
			final AtomicInteger normalListenerCalled = new AtomicInteger(0);

			IEventListener<TestEvent> throwingListener = event -> {
				throw new RuntimeException("Test exception");
			};

			IEventListener<TestEvent> normalListener = event -> {
				normalListenerCalled.incrementAndGet();
			};

			eventSystem.subscribe(TestEvent.class, throwingListener);
			eventSystem.subscribe(TestEvent.class, normalListener);

			// Publish should not crash
			assertDoesNotThrow(() -> eventSystem.publish(new TestEvent("Test")));

			// Normal listener should still be called
			assertEquals(1, normalListenerCalled.get());
		}
	}
}