package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import static org.junit.Assert.*;

import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.ITriggerListener;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

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
 * Comprehensive test suite for the EventSystem
 */
public class EventSystemTest {
	private Scene scene;
	private EventSystem eventSystem;
	private CollisionListenerSystem listenerSystem;

	@Before
	public void setup() {
		scene = new Scene("TestScene");
		Scene.setActiveScene(scene);
		eventSystem = EventSystem.getInstance();
		eventSystem.clear(); // Start with a clean event system
		listenerSystem = new CollisionListenerSystem();
		listenerSystem.start();
	}

	@After
	public void cleanup() {
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

	//========================= Basic EventSystem Functionality Tests =========================

	@Test
	public void testBasicPublishSubscribe() {
		final AtomicInteger callCount = new AtomicInteger(0);
		final String testMessage = "Hello, EventSystem!";

		// Create and subscribe a listener
		IEventListener<TestEvent> listener = event -> {
			callCount.incrementAndGet();
			assertEquals(testMessage, event.getMessage());
		};

		eventSystem.subscribe(TestEvent.class, listener);

		// Publish an event
		eventSystem.publish(new TestEvent(testMessage));

		// Verify listener was called once
		assertEquals(1, callCount.get());

		// Publish again
		eventSystem.publish(new TestEvent(testMessage));

		// Verify listener was called again
		assertEquals(2, callCount.get());
	}

	@Test
	public void testUnsubscribe() {
		final AtomicInteger callCount = new AtomicInteger(0);

		// Create and subscribe a listener
		IEventListener<TestEvent> listener = event -> callCount.incrementAndGet();
		eventSystem.subscribe(TestEvent.class, listener);

		// Publish an event
		eventSystem.publish(new TestEvent("Test"));
		assertEquals(1, callCount.get());

		// Unsubscribe and publish again
		eventSystem.unsubscribe(TestEvent.class, listener);
		eventSystem.publish(new TestEvent("Test"));

		// Verify listener was not called again
		assertEquals(1, callCount.get());
	}

	@Test
	public void testMultipleListeners() {
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);

		// Create and subscribe two listeners
		IEventListener<TestEvent> listener1 = event -> count1.incrementAndGet();
		IEventListener<TestEvent> listener2 = event -> count2.incrementAndGet();

		eventSystem.subscribe(TestEvent.class, listener1);
		eventSystem.subscribe(TestEvent.class, listener2);

		// Publish an event
		eventSystem.publish(new TestEvent("Test"));

		// Verify both listeners were called
		assertEquals(1, count1.get());
		assertEquals(1, count2.get());

		// Unsubscribe one listener
		eventSystem.unsubscribe(TestEvent.class, listener1);
		eventSystem.publish(new TestEvent("Test"));

		// Verify only listener2 was called
		assertEquals(1, count1.get());
		assertEquals(2, count2.get());
	}

	@Test
	public void testClearRemovesAllListeners() {
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);

		// Subscribe to different event types
		IEventListener<TestEvent> listener1 = event -> count1.incrementAndGet();
		IEventListener<CollisionEnterEvent> listener2 = event -> count2.incrementAndGet();

		eventSystem.subscribe(TestEvent.class, listener1);
		eventSystem.subscribe(CollisionEnterEvent.class, listener2);

		// Publish events
		eventSystem.publish(new TestEvent("Test"));
		assertEquals(1, count1.get());

		Entity entity = new Entity();
		Entity other = new Entity();
		eventSystem.publish(new CollisionEnterEvent(entity, other, null));
		assertEquals(1, count2.get());

		// Clear all listeners
		eventSystem.clear();

		// Publish events again
		eventSystem.publish(new TestEvent("Test"));
		eventSystem.publish(new CollisionEnterEvent(entity, other, null));

		// Verify no callbacks were made
		assertEquals(1, count1.get());
		assertEquals(1, count2.get());
	}

	@Test
	public void testPublishNoSubscribers() {
		// This shouldn't throw an exception
		eventSystem.publish(new TestEvent("No subscribers"));

		// Now add a subscriber to a different event type
		final AtomicInteger count = new AtomicInteger(0);
		eventSystem.subscribe(CollisionEnterEvent.class, event -> count.incrementAndGet());

		// Publish event with no subscribers
		eventSystem.publish(new TestEvent("Still no subscribers"));

		// Verify the other subscriber wasn't called
		assertEquals(0, count.get());
	}

	//========================= Collision Event Tests =========================

	@Test
	public void testCollisionEnterEvent() {
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
	public void testCollisionStayEvent() {
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
	public void testCollisionExitEvent() {
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
	public void testContactNormalDirection() {
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
	public void testEntityRemovalStopsCollisionEvents() {
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

	//========================= Trigger Event Tests =========================

	@Test
	public void testTriggerEnterEvent() {
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
	public void testTriggerStayEvent() {
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
	public void testTriggerExitEvent() {
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
	public void testEntityRemovalStopsTriggerEvents() {
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

	//========================= Entity Lifecycle Tests =========================

	@Test
	public void testMultipleEntitiesWithListeners() {
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
			assertEquals("Removed entity should not receive events",
				0, listeners.get(i).collisionEnterCount.get());
		}

		for (int i = entityCount / 2; i < entityCount; i++) {
			assertEquals("Remaining entity should receive events",
				1, listeners.get(i).collisionEnterCount.get());
		}
	}

	@Test
	public void testSceneSwitchingIsolation() {
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
	public void testEntityMovedBetweenScenes() {
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

	//========================= Edge Case Tests =========================

	@Test
	public void testRapidEntityCreationDestruction() {
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
				assertEquals("Removed entity should not receive events",
					0, listeners.get(i).collisionEnterCount.get());
			} else {
				assertEquals("Active entity should receive events",
					1, listeners.get(i).collisionEnterCount.get());
			}
		}
	}

	@Test
	public void testComponentRemoval() {
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
	public void testConcurrentEventProcessing() throws InterruptedException {
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
					e.printStackTrace();
				}
			}).start();
		}

		// Start all threads simultaneously
		startLatch.countDown();

		// Wait for all threads to finish (with timeout)
		finishLatch.await(5, TimeUnit.SECONDS);

		// Verify all events were received exactly once
		for (int i = 0; i < entityCount; i++) {
			assertEquals("Each entity should receive exactly one event",
				1, listeners.get(i).collisionEnterCount.get());
		}
	}

	@Test
	public void testExceptionInListenerDoesNotAffectOtherListeners() {
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
		eventSystem.publish(new TestEvent("Test"));

		// Normal listener should still be called
		assertEquals(1, normalListenerCalled.get());
	}
}