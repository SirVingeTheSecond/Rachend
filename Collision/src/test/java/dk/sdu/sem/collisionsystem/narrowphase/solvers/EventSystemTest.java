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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive test suite for the EventSystem using JUnit 5
 */
@DisplayName("EventSystem Test Suite")
public class EventSystemTest {
	private Scene scene;
	private EventSystem eventSystem;
	private CollisionListenerSystem listenerSystem;

	@BeforeAll
	static void initJavaFX() throws InterruptedException {
		if (!Platform.isFxApplicationThread()) {
			CountDownLatch latch = new CountDownLatch(1);
			Platform.startup(latch::countDown);
			if (!latch.await(5, TimeUnit.SECONDS)) {
				throw new RuntimeException("JavaFX initialization timeout");
			}
		}
	}

	@BeforeEach
	void setup() {
		scene = new Scene("TestScene");
		Scene.setActiveScene(scene);
		eventSystem = EventSystem.getInstance();
		eventSystem.clear();
		listenerSystem = new CollisionListenerSystem();
		listenerSystem.start();
	}

	@AfterEach
	void cleanup() {
		eventSystem.clear();
		scene.clear();
	}

	private void waitForFxEvents() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> future = new CompletableFuture<>();
		Platform.runLater(() -> future.complete(null));
		future.get(5, TimeUnit.SECONDS);
	}

	private CountDownLatch createEventLatch(int expectedEvents) {
		return new CountDownLatch(expectedEvents);
	}

	/**
	 * Basic listener component that implements ICollisionListener and ITriggerListener
	 */
	private static class TestListener implements IComponent, ICollisionListener, ITriggerListener {
		public final AtomicInteger collisionEnterCount = new AtomicInteger(0);
		public final AtomicInteger collisionStayCount = new AtomicInteger(0);
		public final AtomicInteger collisionExitCount = new AtomicInteger(0);

		public final AtomicInteger triggerEnterCount = new AtomicInteger(0);
		public final AtomicInteger triggerStayCount = new AtomicInteger(0);
		public final AtomicInteger triggerExitCount = new AtomicInteger(0);

		public final List<Entity> collidedEntities = new ArrayList<>();
		public final List<Entity> triggeredEntities = new ArrayList<>();

		public ContactPoint lastContact;

		private CountDownLatch eventLatch;

		public void setEventLatch(CountDownLatch latch) {
			this.eventLatch = latch;
		}

		private void signalEvent() {
			if (eventLatch != null) {
				eventLatch.countDown();
			}
		}

		@Override
		public void onCollisionEnter(CollisionEnterEvent event) {
			collisionEnterCount.incrementAndGet();
			collidedEntities.add(event.getOther());
			lastContact = event.getContact();
			signalEvent();
		}

		@Override
		public void onCollisionStay(CollisionStayEvent event) {
			collisionStayCount.incrementAndGet();
			lastContact = event.getContact();
			signalEvent();
		}

		@Override
		public void onCollisionExit(CollisionExitEvent event) {
			collisionExitCount.incrementAndGet();
			lastContact = event.getContact();
			signalEvent();
		}

		@Override
		public void onTriggerEnter(TriggerEnterEvent event) {
			triggerEnterCount.incrementAndGet();
			triggeredEntities.add(event.getOther());
			signalEvent();
		}

		@Override
		public void onTriggerStay(TriggerStayEvent event) {
			triggerStayCount.incrementAndGet();
			signalEvent();
		}

		@Override
		public void onTriggerExit(TriggerExitEvent event) {
			triggerExitCount.incrementAndGet();
			signalEvent();
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

			IEventListener<TestEvent> listener = event -> {
				callCount.incrementAndGet();
				assertEquals(testMessage, event.getMessage());
				latch.countDown();
			};

			eventSystem.subscribe(TestEvent.class, listener);
			eventSystem.publish(new TestEvent(testMessage));

			assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handler should complete within timeout");
			assertEquals(1, callCount.get());

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

			IEventListener<TestEvent> listener = event -> {
				callCount.incrementAndGet();
				latch.countDown();
			};

			eventSystem.subscribe(TestEvent.class, listener);
			eventSystem.publish(new TestEvent("Test"));
			assertTrue(latch.await(5, TimeUnit.SECONDS));
			assertEquals(1, callCount.get());

			eventSystem.unsubscribe(TestEvent.class, listener);
			eventSystem.publish(new TestEvent("Test"));

			// Platform.runLater may still be running, need to ensure it's done
			Thread.sleep(100);

			assertEquals(1, callCount.get());
		}

		@Test
		@DisplayName("Should support multiple listeners for the same event type")
		void testMultipleListeners() throws InterruptedException {
			final AtomicInteger count1 = new AtomicInteger(0);
			final AtomicInteger count2 = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(2);

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

			eventSystem.publish(new TestEvent("Test"));

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(1, count1.get());
			assertEquals(1, count2.get());

			final CountDownLatch latch2 = new CountDownLatch(1);
			final AtomicInteger count1_after = new AtomicInteger(count1.get());
			final AtomicInteger count2_after = new AtomicInteger(count2.get());

			eventSystem.unsubscribe(TestEvent.class, listener1);

			eventSystem.unsubscribe(TestEvent.class, listener2);
			IEventListener<TestEvent> newListener2 = event -> {
				count2_after.incrementAndGet();
				latch2.countDown();
			};
			eventSystem.subscribe(TestEvent.class, newListener2);

			eventSystem.publish(new TestEvent("Test"));
			assertTrue(latch2.await(5, TimeUnit.SECONDS));

			assertEquals(1, count1_after.get());
			assertEquals(2, count2_after.get());
		}

		@Test
		@DisplayName("Should clear all listeners when clear() is called")
		void testClearRemovesAllListeners() throws InterruptedException, ExecutionException, TimeoutException {
			final AtomicInteger count1 = new AtomicInteger(0);
			final AtomicInteger count2 = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(2);

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

			eventSystem.publish(new TestEvent("Test"));
			Entity entity = new Entity();
			Entity other = new Entity();
			eventSystem.publish(new CollisionEnterEvent(entity, other, null));

			assertTrue(latch.await(5, TimeUnit.SECONDS));
			assertEquals(1, count1.get());
			assertEquals(1, count2.get());

			eventSystem.clear();

			eventSystem.publish(new TestEvent("Test"));
			eventSystem.publish(new CollisionEnterEvent(entity, other, null));

			waitForFxEvents();
			Thread.sleep(100);

			assertEquals(1, count1.get());
			assertEquals(1, count2.get());
		}
	}

	@Nested
	@DisplayName("Collision Event Tests")
	class CollisionEventTests {

		@Test
		@DisplayName("Should handle collision enter events correctly")
		void testCollisionEnterEvent() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(1);
			listener.setEventLatch(latch);

			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);
			CollisionEnterEvent enterEvent = new CollisionEnterEvent(entity, otherEntity, contact);

			eventSystem.publish(enterEvent);

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.collisionEnterCount.get());
			assertTrue(listener.collidedEntities.contains(otherEntity));
			assertEquals(contact, listener.lastContact);
		}

		@Test
		@DisplayName("Should handle collision stay events correctly")
		void testCollisionStayEvent() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(3);
			listener.setEventLatch(latch);

			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);
			CollisionStayEvent stayEvent = new CollisionStayEvent(entity, otherEntity, contact);

			for (int i = 0; i < 3; i++) {
				eventSystem.publish(stayEvent);
			}

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(3, listener.collisionStayCount.get());
		}

		@Test
		@DisplayName("Should handle collision exit events correctly")
		void testCollisionExitEvent() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(1);
			listener.setEventLatch(latch);

			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);
			CollisionExitEvent exitEvent = new CollisionExitEvent(entity, otherEntity, contact);

			eventSystem.publish(exitEvent);

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.collisionExitCount.get());
		}

		@Test
		@DisplayName("Should stop sending collision events when entity is removed")
		void testEntityRemovalStopsCollisionEvents() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch initialLatch = createEventLatch(1);
			listener.setEventLatch(initialLatch);

			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			CollisionEnterEvent enterEvent = new CollisionEnterEvent(entity, otherEntity, contact);
			CollisionStayEvent stayEvent = new CollisionStayEvent(entity, otherEntity, contact);
			CollisionExitEvent exitEvent = new CollisionExitEvent(entity, otherEntity, contact);

			eventSystem.publish(enterEvent);
			assertTrue(initialLatch.await(5, TimeUnit.SECONDS));
			assertEquals(1, listener.collisionEnterCount.get());

			scene.removeEntity(entity);
			listenerSystem.update();

			waitForFxEvents();

			listener.collisionEnterCount.set(0);
			listener.collisionStayCount.set(0);
			listener.collisionExitCount.set(0);

			listener.setEventLatch(null);

			eventSystem.publish(enterEvent);
			eventSystem.publish(stayEvent);
			eventSystem.publish(exitEvent);

			waitForFxEvents();
			Thread.sleep(100);

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
		void testTriggerEnterEvent() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(1);
			listener.setEventLatch(latch);

			entity.addComponent(listener);

			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			TriggerEnterEvent enterEvent = new TriggerEnterEvent(entity, otherEntity);
			eventSystem.publish(enterEvent);

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.triggerEnterCount.get());
			assertTrue(listener.triggeredEntities.contains(otherEntity));
		}

		@Test
		@DisplayName("Should handle trigger stay events correctly")
		void testTriggerStayEvent() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(5);
			listener.setEventLatch(latch);

			entity.addComponent(listener);

			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			TriggerStayEvent stayEvent = new TriggerStayEvent(entity, otherEntity);

			for (int i = 0; i < 5; i++) {
				eventSystem.publish(stayEvent);
			}

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(5, listener.triggerStayCount.get());
		}

		@Test
		@DisplayName("Should handle trigger exit events correctly")
		void testTriggerExitEvent() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(1);
			listener.setEventLatch(latch);

			entity.addComponent(listener);

			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			TriggerExitEvent exitEvent = new TriggerExitEvent(entity, otherEntity);
			eventSystem.publish(exitEvent);

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.triggerExitCount.get());
		}

		@Test
		@DisplayName("Should stop sending trigger events when entity is removed")
		void testEntityRemovalStopsTriggerEvents() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch initialLatch = createEventLatch(1);
			listener.setEventLatch(initialLatch);

			entity.addComponent(listener);

			BoxColliderComponent collider = new BoxColliderComponent(entity, 10, 10);
			collider.setTrigger(true);
			entity.addComponent(collider);
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();
			otherEntity.addComponent(new TransformComponent(new Vector2D(5, 5), 0));
			scene.addEntity(otherEntity);

			TriggerEnterEvent enterEvent = new TriggerEnterEvent(entity, otherEntity);
			TriggerStayEvent stayEvent = new TriggerStayEvent(entity, otherEntity);
			TriggerExitEvent exitEvent = new TriggerExitEvent(entity, otherEntity);

			eventSystem.publish(enterEvent);
			assertTrue(initialLatch.await(5, TimeUnit.SECONDS));
			assertEquals(1, listener.triggerEnterCount.get());

			scene.removeEntity(entity);
			listenerSystem.update();

			waitForFxEvents();

			listener.triggerEnterCount.set(0);
			listener.triggerStayCount.set(0);
			listener.triggerExitCount.set(0);

			listener.setEventLatch(null);

			eventSystem.publish(enterEvent);
			eventSystem.publish(stayEvent);
			eventSystem.publish(exitEvent);

			waitForFxEvents();
			Thread.sleep(100);

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
		void testMultipleEntitiesWithListeners() throws Exception {
			int entityCount = 10;
			List<Entity> entities = new ArrayList<>();
			List<TestListener> listeners = new ArrayList<>();
			CountDownLatch latch = createEventLatch(entityCount);

			for (int i = 0; i < entityCount; i++) {
				Entity entity = new Entity();
				entity.addComponent(new TransformComponent(new Vector2D(i * 10, 0), 0));
				TestListener listener = new TestListener();
				listener.setEventLatch(latch);
				entity.addComponent(listener);
				entity.addComponent(new BoxColliderComponent(entity, 10, 10));
				entity.addComponent(new CollisionStateComponent());

				scene.addEntity(entity);
				entities.add(entity);
				listeners.add(listener);
			}

			listenerSystem.update();

			Entity targetEntity = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			for (Entity entity : entities) {
				CollisionEnterEvent event = new CollisionEnterEvent(entity, targetEntity, contact);
				eventSystem.publish(event);
			}

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			for (TestListener listener : listeners) {
				assertEquals(1, listener.collisionEnterCount.get());
				assertTrue(listener.collidedEntities.contains(targetEntity));
			}
		}

		@Test
		@DisplayName("Should handle entity addition and removal during event processing")
		void testEntityAdditionRemovalDuringEventProcessing() throws Exception {
			Entity existingEntity = new Entity();
			existingEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener existingListener = new TestListener();

			CountDownLatch existingLatch = createEventLatch(1);
			existingListener.setEventLatch(existingLatch);

			existingEntity.addComponent(existingListener);
			existingEntity.addComponent(new BoxColliderComponent(existingEntity, 10, 10));
			existingEntity.addComponent(new CollisionStateComponent());

			scene.addEntity(existingEntity);
			listenerSystem.update();

			Entity newEntity = new Entity();
			newEntity.addComponent(new TransformComponent(new Vector2D(10, 0), 0));
			TestListener newListener = new TestListener();
			newEntity.addComponent(newListener);
			newEntity.addComponent(new BoxColliderComponent(newEntity, 10, 10));
			newEntity.addComponent(new CollisionStateComponent());

			Entity targetEntity = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			CollisionEnterEvent existingEvent = new CollisionEnterEvent(existingEntity, targetEntity, contact);
			eventSystem.publish(existingEvent);

			assertTrue(existingLatch.await(5, TimeUnit.SECONDS));

			scene.addEntity(newEntity);
			listenerSystem.update();

			CountDownLatch bothLatch = createEventLatch(2);
			existingListener.setEventLatch(bothLatch);
			newListener.setEventLatch(bothLatch);

			eventSystem.publish(new CollisionEnterEvent(existingEntity, targetEntity, contact));
			eventSystem.publish(new CollisionEnterEvent(newEntity, targetEntity, contact));

			assertTrue(bothLatch.await(5, TimeUnit.SECONDS));

			assertEquals(2, existingListener.collisionEnterCount.get());
			assertEquals(1, newListener.collisionEnterCount.get());
		}
	}

	@Nested
	@DisplayName("Scene Management Tests")
	class SceneManagementTests {

		@Test
		@DisplayName("Should handle multiple scenes with independent entities")
		void testMultipleScenesIndependentEntities() throws Exception {
			Scene sceneA = new Scene("SceneA");
			Scene sceneB = new Scene("SceneB");

			Entity entityA = new Entity();
			entityA.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listenerA = new TestListener();
			CountDownLatch latchA = createEventLatch(2);
			listenerA.setEventLatch(latchA);
			entityA.addComponent(listenerA);
			entityA.addComponent(new BoxColliderComponent(entityA, 10, 10));
			entityA.addComponent(new CollisionStateComponent());

			Entity entityB = new Entity();
			entityB.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listenerB = new TestListener();
			entityB.addComponent(listenerB);
			entityB.addComponent(new BoxColliderComponent(entityB, 10, 10));
			entityB.addComponent(new CollisionStateComponent());

			Scene.setActiveScene(sceneA);
			sceneA.addEntity(entityA);
			listenerSystem.update();

			Scene.setActiveScene(sceneB);
			sceneB.addEntity(entityB);
			listenerSystem.update();

			Entity targetA = new Entity();
			Entity targetB = new Entity();
			ContactPoint contactPoint = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			CollisionEnterEvent collisionEventA = new CollisionEnterEvent(entityA, targetA, contactPoint);
			TriggerEnterEvent triggerEventA = new TriggerEnterEvent(entityA, targetA);

			CollisionEnterEvent collisionEventB = new CollisionEnterEvent(entityB, targetB, contactPoint);
			TriggerEnterEvent triggerEventB = new TriggerEnterEvent(entityB, targetB);

			Scene.setActiveScene(sceneA);

			eventSystem.publish(collisionEventA);
			eventSystem.publish(triggerEventA);
			eventSystem.publish(collisionEventB);
			eventSystem.publish(triggerEventB);

			assertTrue(latchA.await(5, TimeUnit.SECONDS));
			waitForFxEvents();

			assertEquals(1, listenerA.collisionEnterCount.get());
			assertEquals(1, listenerA.triggerEnterCount.get());
			assertEquals(0, listenerB.collisionEnterCount.get());
			assertEquals(0, listenerB.triggerEnterCount.get());

			listenerA.collisionEnterCount.set(0);
			listenerA.triggerEnterCount.set(0);

			Scene.setActiveScene(sceneB);

			CountDownLatch latchB = createEventLatch(2);
			listenerB.setEventLatch(latchB);

			eventSystem.publish(collisionEventA);
			eventSystem.publish(triggerEventA);
			eventSystem.publish(collisionEventB);
			eventSystem.publish(triggerEventB);

			assertTrue(latchB.await(5, TimeUnit.SECONDS));

			assertEquals(0, listenerA.collisionEnterCount.get());
			assertEquals(0, listenerA.triggerEnterCount.get());
			assertEquals(1, listenerB.collisionEnterCount.get());
			assertEquals(1, listenerB.triggerEnterCount.get());
		}

		@Test
		@DisplayName("Should handle entities moved between scenes correctly")
		void testEntityMovedBetweenScenes() throws Exception {
			Scene sceneA = new Scene("SceneA");
			Scene sceneB = new Scene("SceneB");

			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();
			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			Scene.setActiveScene(sceneA);
			sceneA.addEntity(entity);
			listenerSystem.update();

			Entity target = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(5, 5), new Vector2D(1, 0), 0);

			CollisionEnterEvent collisionEvent = new CollisionEnterEvent(entity, target, contact);
			TriggerEnterEvent triggerEvent = new TriggerEnterEvent(entity, target);

			CountDownLatch latchA = createEventLatch(2);
			listener.setEventLatch(latchA);

			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			assertTrue(latchA.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.collisionEnterCount.get());
			assertEquals(1, listener.triggerEnterCount.get());

			sceneA.removeEntity(entity);
			Scene.setActiveScene(sceneB);
			sceneB.addEntity(entity);
			listenerSystem.update();

			listener.collisionEnterCount.set(0);
			listener.triggerEnterCount.set(0);

			Scene.setActiveScene(sceneA);

			listener.setEventLatch(null);
			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			waitForFxEvents();
			Thread.sleep(100);

			assertEquals(0, listener.collisionEnterCount.get());
			assertEquals(0, listener.triggerEnterCount.get());

			Scene.setActiveScene(sceneB);

			CountDownLatch latchB = createEventLatch(2);
			listener.setEventLatch(latchB);

			eventSystem.publish(collisionEvent);
			eventSystem.publish(triggerEvent);

			assertTrue(latchB.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.collisionEnterCount.get());
			assertEquals(1, listener.triggerEnterCount.get());
		}
	}

	@Nested
	@DisplayName("Edge Case Tests")
	class EdgeCaseTests {

		@Test
		@DisplayName("Should handle rapid entity creation and destruction")
		void testRapidEntityCreationDestruction() throws Exception {
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

				// verifies system handles fast lifecycle changes
				if (i % 2 == 0) {
					scene.removeEntity(entity);
				}
			}

			listenerSystem.update();

			int expectedActiveEntities = totalEntities / 2;
			CountDownLatch latch = createEventLatch(expectedActiveEntities);

			for (int i = 0; i < totalEntities; i++) {
				if (i % 2 != 0) {
					listeners.get(i).setEventLatch(latch);
				}
			}

			Entity target = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			for (int i = 0; i < totalEntities; i++) {
				CollisionEnterEvent event = new CollisionEnterEvent(entities.get(i), target, contact);
				eventSystem.publish(event);
			}

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			for (int i = 0; i < totalEntities; i++) {
				if (i % 2 == 0) {
					assertEquals(0, listeners.get(i).collisionEnterCount.get());
				} else {
					assertEquals(1, listeners.get(i).collisionEnterCount.get());
				}
			}
		}

		@Test
		@DisplayName("Should handle null contact points gracefully")
		void testNullContactPoints() throws Exception {
			Entity entity = new Entity();
			entity.addComponent(new TransformComponent(new Vector2D(0, 0), 0));
			TestListener listener = new TestListener();

			CountDownLatch latch = createEventLatch(3);
			listener.setEventLatch(latch);

			entity.addComponent(listener);
			entity.addComponent(new BoxColliderComponent(entity, 10, 10));
			entity.addComponent(new CollisionStateComponent());

			scene.addEntity(entity);
			listenerSystem.update();

			Entity otherEntity = new Entity();

			CollisionEnterEvent enterEvent = new CollisionEnterEvent(entity, otherEntity, null);
			CollisionStayEvent stayEvent = new CollisionStayEvent(entity, otherEntity, null);
			CollisionExitEvent exitEvent = new CollisionExitEvent(entity, otherEntity, null);

			eventSystem.publish(enterEvent);
			eventSystem.publish(stayEvent);
			eventSystem.publish(exitEvent);

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(1, listener.collisionEnterCount.get());
			assertEquals(1, listener.collisionStayCount.get());
			assertEquals(1, listener.collisionExitCount.get());
			assertNull(listener.lastContact);
		}

		@Test
		@DisplayName("Should handle event publish with no listeners")
		void testEventPublishWithNoListeners() throws Exception {
			Entity entity = new Entity();
			Entity otherEntity = new Entity();
			ContactPoint contact = new ContactPoint(new Vector2D(0, 0), new Vector2D(1, 0), 0);

			CollisionEnterEvent enterEvent = new CollisionEnterEvent(entity, otherEntity, contact);
			eventSystem.publish(enterEvent);

			waitForFxEvents();

			TestEvent testEvent = new TestEvent("No listeners");
			eventSystem.publish(testEvent);

			waitForFxEvents();

			// If no exceptions thrown, test passes - verifies graceful handling of no listeners
		}

		@Test
		@DisplayName("Should handle listener exceptions without affecting other listeners")
		void testListenerExceptionHandling() throws Exception {
			final AtomicInteger successfulListenerCount = new AtomicInteger(0);
			final CountDownLatch latch = new CountDownLatch(2);

			IEventListener<TestEvent> failingListener = event -> {
				throw new RuntimeException("Test exception");
			};

			IEventListener<TestEvent> successfulListener1 = event -> {
				successfulListenerCount.incrementAndGet();
				latch.countDown();
			};

			IEventListener<TestEvent> successfulListener2 = event -> {
				successfulListenerCount.incrementAndGet();
				latch.countDown();
			};

			eventSystem.subscribe(TestEvent.class, failingListener);
			eventSystem.subscribe(TestEvent.class, successfulListener1);
			eventSystem.subscribe(TestEvent.class, successfulListener2);

			eventSystem.publish(new TestEvent("Test"));

			assertTrue(latch.await(5, TimeUnit.SECONDS));

			assertEquals(2, successfulListenerCount.get());
		}
	}
}