package dk.sdu.sem.collisionsystem.raycasting;

import dk.sdu.sem.collision.components.BoxColliderComponent;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.TilemapColliderComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.collision.shapes.GridShape;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.collisionsystem.nodes.TilemapColliderNode;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.commontilemap.TilemapComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.player.PlayerComponent;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Raycast Tests")
public class RaycastTest {

	private RaycastHandler raycastHandler;
	private Scene testScene;
	private NodeManager mockNodeManager;

	// Test entities
	private Entity boxEntity;
	private Entity circleEntity;
	private Entity playerEntity;
	private Entity tilemapEntity;

	@BeforeEach
	public void setUp() {
		raycastHandler = new RaycastHandler();
		testScene = new Scene("TestScene");
		mockNodeManager = mock(NodeManager.class);

		// Set the active scene
		Scene.setActiveScene(testScene);

		// Set up a method to return our mock NodeManager
		try {
			Field field = NodeManager.class.getDeclaredField("active");
			field.setAccessible(true);
			field.set(null, mockNodeManager);
		} catch (Exception e) {
			fail("Failed to mock NodeManager.active(): " + e.getMessage());
		}

		// Create test entities
		createTestEntities();
	}

	private void createTestEntities() {
		// Create a box entity
		boxEntity = new Entity();
		boxEntity.addComponent(new TransformComponent(new Vector2D(100, 100), 0, new Vector2D(1, 1)));
		BoxColliderComponent boxCollider = new BoxColliderComponent(boxEntity, new Vector2D(0, 0), 50, 50, false, PhysicsLayer.OBSTACLE);
		boxEntity.addComponent(boxCollider);
		testScene.addEntity(boxEntity);

		// Create a circle entity
		circleEntity = new Entity();
		circleEntity.addComponent(new TransformComponent(new Vector2D(200, 100), 0, new Vector2D(1, 1)));
		CircleColliderComponent circleCollider = new CircleColliderComponent(circleEntity, new Vector2D(0, 0), 25, false, PhysicsLayer.ENEMY);
		circleEntity.addComponent(circleCollider);
		testScene.addEntity(circleEntity);

		// Create a player entity
		playerEntity = new Entity();
		playerEntity.addComponent(new PlayerComponent());
		playerEntity.addComponent(new TransformComponent(new Vector2D(50, 50), 0, new Vector2D(1, 1)));
		CircleColliderComponent playerCollider = new CircleColliderComponent(playerEntity, new Vector2D(0, 0), 20, false, PhysicsLayer.PLAYER);
		playerEntity.addComponent(playerCollider);
		testScene.addEntity(playerEntity);

		// Create a tilemap entity
		tilemapEntity = new Entity();
		tilemapEntity.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Create collision grid (1 = solid, 0 = not solid)
		int[][] collisionGrid = new int[10][10];
		// Add a wall at x=5
		for (int y = 0; y < 10; y++) {
			collisionGrid[5][y] = 1;
		}

		// Create tilemap components
		TilemapComponent tilemapComponent = new TilemapComponent("test_map", collisionGrid, GameConstants.TILE_SIZE);
		tilemapEntity.addComponent(tilemapComponent);

		GridShape gridShape = new GridShape(collisionGrid, GameConstants.TILE_SIZE);
		TilemapColliderComponent tilemapCollider = new TilemapColliderComponent(tilemapEntity, tilemapComponent, collisionGrid);
		tilemapCollider.setLayer(PhysicsLayer.OBSTACLE);
		tilemapEntity.addComponent(tilemapCollider);

		testScene.addEntity(tilemapEntity);
	}

	@Test
	@DisplayName("Test raycast with no colliders")
	public void testRaycastNoColliders() {
		// No colliders in the scene
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(Collections.emptySet());
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(0, 0),
			new Vector2D(1, 0),
			100
		);

		// Verify no hit
		assertFalse(hit.isHit(), "Ray should not hit anything");
		assertNull(hit.getEntity(), "Hit entity should be null");
		assertEquals(0, hit.getDistance(), "Hit distance should be 0");
	}

	@Test
	@DisplayName("Test raycast against box - direct hit")
	public void testRaycastBoxDirectHit() {
		// Create mock nodes for box
		ColliderNode boxNode = createColliderNode(boxEntity);
		Set<ColliderNode> colliderNodes = Collections.singleton(boxNode);

		// Configure NodeManager to return our box
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that should hit the box
		// Box is at (100,100) with size 50x50, so aim at (125,125)
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(0, 125),  // From left of box
			new Vector2D(1, 0),   // Going right
			200                   // Long enough to hit
		);

		// Verify hit
		assertTrue(hit.isHit(), "Ray should hit the box");
		assertEquals(boxEntity, hit.getEntity(), "Should hit box entity");
		assertTrue(hit.getDistance() > 0, "Hit distance should be positive");
		assertNotNull(hit.getPoint(), "Hit point should not be null");
		assertNotNull(hit.getNormal(), "Hit normal should not be null");
	}

	@Test
	@DisplayName("Test raycast against box - near miss")
	public void testRaycastBoxNearMiss() {
		// Create mock nodes for box
		ColliderNode boxNode = createColliderNode(boxEntity);
		Set<ColliderNode> colliderNodes = Collections.singleton(boxNode);

		// Configure NodeManager to return our box
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that should miss the box slightly
		// Box is at (100,100) with size 50x50, so aim just above at y=90
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(0, 90),   // From left, above box
			new Vector2D(1, 0),    // Going right
			200                    // Long enough to pass box
		);

		// Verify miss
		assertFalse(hit.isHit(), "Ray should miss the box");
	}

	@Test
	@DisplayName("Test raycast against circle - direct hit")
	public void testRaycastCircleDirectHit() {
		// Create mock nodes for circle
		ColliderNode circleNode = createColliderNode(circleEntity);
		Set<ColliderNode> colliderNodes = Collections.singleton(circleNode);

		// Configure NodeManager to return our circle
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that should hit the circle
		// Circle is at (200,100) with radius 25
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(150, 100),  // From left of circle
			new Vector2D(1, 0),      // Going right
			100                      // Long enough to hit
		);

		// Verify hit
		assertTrue(hit.isHit(), "Ray should hit the circle");
		assertEquals(circleEntity, hit.getEntity(), "Should hit circle entity");
		assertTrue(hit.getDistance() > 0, "Hit distance should be positive");
		assertNotNull(hit.getPoint(), "Hit point should not be null");
		assertNotNull(hit.getNormal(), "Hit normal should not be null");
	}

	@Test
	@DisplayName("Test raycast against tilemap - direct hit")
	public void testRaycastTilemapDirectHit() {
		// Create mock node for tilemap
		TilemapColliderNode tilemapNode = createTilemapColliderNode(tilemapEntity);
		Set<TilemapColliderNode> tilemapNodes = Collections.singleton(tilemapNode);

		// Configure NodeManager to return our tilemap
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(Collections.emptySet());
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(tilemapNodes);

		// Cast ray that should hit the tilemap wall at x=5 (5*24 = 120 pixels)
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(50, 50),   // From left of wall
			new Vector2D(1, 0),     // Going right
			200                     // Long enough to hit
		);

		// Verify hit
		assertTrue(hit.isHit(), "Ray should hit the tilemap wall");
		assertEquals(tilemapEntity, hit.getEntity(), "Should hit tilemap entity");
		assertTrue(hit.getDistance() > 0, "Hit distance should be positive");
		assertNotNull(hit.getPoint(), "Hit point should not be null");
		assertNotNull(hit.getNormal(), "Hit normal should not be null");

		// Check hit details
		assertEquals(-1, hit.getNormal().x(), "Normal should point left (away from wall)");
		assertEquals(0, hit.getNormal().y(), "Normal Y component should be 0");

		// Distance should be approximately (120 - 50) = 70
		assertTrue(Math.abs(hit.getDistance() - 70) < 1,
			"Distance should be approximately 70, got: " + hit.getDistance());
	}

	@Test
	@DisplayName("Test raycast with layer filter - matching layer")
	public void testRaycastWithLayerFilterMatch() {
		// Set up entities in the scene
		ColliderNode boxNode = createColliderNode(boxEntity);  // OBSTACLE layer
		ColliderNode circleNode = createColliderNode(circleEntity);  // ENEMY layer
		Set<ColliderNode> colliderNodes = new HashSet<>(Arrays.asList(boxNode, circleNode));

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that should only hit ENEMY layer
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(150, 100),  // Between box and circle
			new Vector2D(1, 0),      // Going right
			100,                     // Long enough to hit circle
			PhysicsLayer.ENEMY       // Only hit ENEMY layer
		);

		// Verify hit circle only
		assertTrue(hit.isHit(), "Ray should hit the circle");
		assertEquals(circleEntity, hit.getEntity(), "Should hit circle entity");
	}

	@Test
	@DisplayName("Test raycast with layer filter - non-matching layer")
	public void testRaycastWithLayerFilterNoMatch() {
		// Set up entities in the scene
		ColliderNode boxNode = createColliderNode(boxEntity);  // OBSTACLE layer
		ColliderNode circleNode = createColliderNode(circleEntity);  // ENEMY layer
		Set<ColliderNode> colliderNodes = new HashSet<>(Arrays.asList(boxNode, circleNode));

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that should only hit PLAYER layer (neither box nor circle)
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(50, 100),   // Left of both entities
			new Vector2D(1, 0),      // Going right
			300,                     // Long enough to pass both
			PhysicsLayer.PLAYER      // Only hit PLAYER layer
		);

		// Verify no hit
		assertFalse(hit.isHit(), "Ray should not hit anything");
	}

	@Test
	@DisplayName("Test raycast with multiple layer filter")
	public void testRaycastWithMultipleLayerFilter() {
		// Set up entities in the scene
		ColliderNode boxNode = createColliderNode(boxEntity);  // OBSTACLE layer
		ColliderNode playerNode = createColliderNode(playerEntity);  // PLAYER layer
		Set<ColliderNode> colliderNodes = new HashSet<>(Arrays.asList(boxNode, playerNode));

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that should hit either OBSTACLE or PLAYER layer
		// Player is at (50,50), box is at (100,100)
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(25, 50),   // Left of player
			new Vector2D(1, 0),     // Going right
			200,                    // Long enough to hit both
			List.of(PhysicsLayer.OBSTACLE, PhysicsLayer.PLAYER)  // Hit either layer
		);

		// Verify hit player (closest)
		assertTrue(hit.isHit(), "Ray should hit something");
		assertEquals(playerEntity, hit.getEntity(), "Should hit player entity (closest)");
	}

	@Test
	@DisplayName("Test raycast with maximum distance limit")
	public void testRaycastMaxDistance() {
		// Create mock nodes for box
		ColliderNode boxNode = createColliderNode(boxEntity);
		Set<ColliderNode> colliderNodes = Collections.singleton(boxNode);

		// Configure NodeManager to return our box
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Box is at (100,100), so a ray from (0,100) needs >100 distance to hit

		// Test with too short distance
		RaycastHit shortHit = raycastHandler.raycast(
			new Vector2D(0, 100),  // From left of box
			new Vector2D(1, 0),    // Going right
			50                     // Too short to hit
		);

		// Verify no hit (distance too short)
		assertFalse(shortHit.isHit(), "Ray with short distance should not hit");

		// Test with sufficient distance
		RaycastHit longHit = raycastHandler.raycast(
			new Vector2D(0, 100),  // From left of box
			new Vector2D(1, 0),    // Going right
			150                    // Long enough to hit
		);

		// Verify hit
		assertTrue(longHit.isHit(), "Ray with sufficient distance should hit");
		assertEquals(boxEntity, longHit.getEntity(), "Should hit box entity");
	}

	@Test
	@DisplayName("Test raycast through multiple objects")
	public void testRaycastMultipleObjects() {
		// Position entities in line: box at (100,100), circle at (200,100)
		boxEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(100, 100));
		circleEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(200, 100));

		// Create mock nodes
		ColliderNode boxNode = createColliderNode(boxEntity);
		ColliderNode circleNode = createColliderNode(circleEntity);
		Set<ColliderNode> colliderNodes = new HashSet<>(Arrays.asList(boxNode, circleNode));

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Cast ray that passes through both
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(0, 100),  // From left of both
			new Vector2D(1, 0),    // Going right
			300                    // Long enough to hit both
		);

		// Verify hit box (closer one)
		assertTrue(hit.isHit(), "Ray should hit something");
		assertEquals(boxEntity, hit.getEntity(), "Should hit box entity (closest)");
	}

	@Test
	@DisplayName("Test raycast against both standard and tilemap colliders")
	public void testRaycastWithBothTypes() {
		// Position entities: box at (150,100), tilemap wall at x=5 (120px)
		boxEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(150, 100));

		// Create mock nodes
		ColliderNode boxNode = createColliderNode(boxEntity);
		Set<ColliderNode> colliderNodes = Collections.singleton(boxNode);

		TilemapColliderNode tilemapNode = createTilemapColliderNode(tilemapEntity);
		Set<TilemapColliderNode> tilemapNodes = Collections.singleton(tilemapNode);

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(tilemapNodes);

		// Cast ray that would hit both
		RaycastHit hit = raycastHandler.raycast(
			new Vector2D(50, 100),  // From left of both
			new Vector2D(1, 0),     // Going right
			200                     // Long enough to hit both
		);

		// Verify hit tilemap (closer)
		assertTrue(hit.isHit(), "Ray should hit something");
		assertEquals(tilemapEntity, hit.getEntity(), "Should hit tilemap entity (closest)");
	}

	/**
	 * Special test to check for issues with the MeleeSystem scenario
	 */
	@Test
	@DisplayName("Test MeleeSystem scenario with multiple layers")
	public void testMeleeSystemScenario() {
		// Position entities: player at (50,50), enemy at (200,100), box at (125,75)
		playerEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(50, 50));
		circleEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(200, 100));
		boxEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(125, 75));

		// Create mock nodes
		ColliderNode playerNode = createColliderNode(playerEntity);
		ColliderNode circleNode = createColliderNode(circleEntity);
		ColliderNode boxNode = createColliderNode(boxEntity);
		Set<ColliderNode> colliderNodes = new HashSet<>(Arrays.asList(playerNode, circleNode, boxNode));

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Test OBSTACLE-only raycast (MeleeSystem's original approach)
		RaycastHit obstacleOnlyHit = raycastHandler.raycast(
			new Vector2D(50, 50),   // From player
			new Vector2D(0.8f, 0.6f).normalize(), // Toward enemy
			200,                    // Long enough to reach enemy
			List.of(PhysicsLayer.OBSTACLE)       // Only check OBSTACLE layer
		);

		// This should hit the box (obstacle)
		assertTrue(obstacleOnlyHit.isHit(), "Ray should hit the obstacle");
		assertEquals(boxEntity, obstacleOnlyHit.getEntity(), "Should hit the box entity");

		// Test multi-layer raycast (EnemySystem approach)
		RaycastHit multiLayerHit = raycastHandler.raycast(
			new Vector2D(50, 50),   // From player
			new Vector2D(0.8f, 0.6f).normalize(), // Toward enemy
			200,                    // Long enough to reach enemy
			List.of(PhysicsLayer.OBSTACLE, PhysicsLayer.ENEMY) // Check both layers
		);

		// This should still hit the box (obstacle) which is closer
		assertTrue(multiLayerHit.isHit(), "Ray should hit something");
		assertEquals(boxEntity, multiLayerHit.getEntity(), "Should hit the box entity (closest)");
		assertNotEquals(circleEntity, multiLayerHit.getEntity(), "Should NOT hit the enemy entity (behind obstacle)");
	}

	@Test
	@DisplayName("Test MeleeSystem clear path scenario")
	public void testMeleeSystemClearPathScenario() {
		// Position entities: player at (50,50), enemy at (200,100), NO obstacle
		playerEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(50, 50));
		circleEntity.getComponent(TransformComponent.class).setPosition(new Vector2D(200, 100));

		// Create mock nodes (no box/obstacle)
		ColliderNode playerNode = createColliderNode(playerEntity);
		ColliderNode circleNode = createColliderNode(circleEntity);
		Set<ColliderNode> colliderNodes = new HashSet<>(Arrays.asList(playerNode, circleNode));

		// Configure NodeManager to return our nodes
		when(mockNodeManager.getNodes(ColliderNode.class)).thenReturn(colliderNodes);
		when(mockNodeManager.getNodes(TilemapColliderNode.class)).thenReturn(Collections.emptySet());

		// Test OBSTACLE-only raycast (MeleeSystem's original approach)
		RaycastHit obstacleOnlyHit = raycastHandler.raycast(
			new Vector2D(50, 50),   // From player
			new Vector2D(0.8f, 0.6f).normalize(), // Toward enemy
			200,                    // Long enough to reach enemy
			List.of(PhysicsLayer.OBSTACLE)       // Only check OBSTACLE layer
		);

		// This should NOT hit anything (no obstacles)
		assertFalse(obstacleOnlyHit.isHit(), "Ray should not hit any obstacles");

		// Test multi-layer raycast (our proposed solution)
		RaycastHit multiLayerHit = raycastHandler.raycast(
			new Vector2D(50, 50),   // From player
			new Vector2D(0.8f, 0.6f).normalize(), // Toward enemy
			200,                    // Long enough to reach enemy
			List.of(PhysicsLayer.OBSTACLE, PhysicsLayer.ENEMY) // Check both layers
		);

		// This should hit the enemy
		assertTrue(multiLayerHit.isHit(), "Ray should hit the enemy");
		assertEquals(circleEntity, multiLayerHit.getEntity(), "Should hit the enemy entity");
	}

	/**
	 * Helper method to create a ColliderNode for an entity
	 */
	private ColliderNode createColliderNode(Entity entity) {
		ColliderNode node = new ColliderNode();
		node.initialize(entity);
		node.transform = entity.getComponent(TransformComponent.class);
		node.collider = entity.getComponent(BoxColliderComponent.class);
		if (node.collider == null) {
			node.collider = entity.getComponent(CircleColliderComponent.class);
		}
		return node;
	}

	/**
	 * Helper method to create a TilemapColliderNode
	 */
	private TilemapColliderNode createTilemapColliderNode(Entity entity) {
		TilemapColliderNode node = new TilemapColliderNode();
		node.initialize(entity);
		node.transform = entity.getComponent(TransformComponent.class);
		node.tilemap = entity.getComponent(TilemapComponent.class);
		node.collider = entity.getComponent(TilemapColliderComponent.class);
		return node;
	}
}