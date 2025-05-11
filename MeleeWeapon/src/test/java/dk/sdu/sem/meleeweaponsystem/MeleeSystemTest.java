package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.data.RaycastHit;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.WeaponDamage;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.player.PlayerComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MeleeSystemTest {

	// Mock collision service
	private ICollisionSPI mockCollisionService;

	// System under test
	private MeleeSystem meleeSystem;

	// Test entities
	private Entity player;
	private Entity enemy;
	private Entity meleeEffect;

	@BeforeEach
	public void setUp() throws Exception {
		mockCollisionService = mock(ICollisionSPI.class);

		meleeSystem = new MeleeSystem();

		// Reflection to inject the mock
		Field field = MeleeSystem.class.getDeclaredField("collisionService");
		field.setAccessible(true);
		field.set(meleeSystem, mockCollisionService);

		setupTestScene();
	}

	private void setupTestScene() {
		// player
		player = new Entity();
		player.addComponent(new PlayerComponent());
		player.addComponent(new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// enemy
		enemy = new Entity();
		enemy.addComponent(new TransformComponent(new Vector2D(100, 0), 0, new Vector2D(1, 1)));

		// melee effect
		meleeEffect = new Entity();
		meleeEffect.addComponent(new TransformComponent(new Vector2D(20, 0), 0, new Vector2D(1, 1)));
		meleeEffect.addComponent(new MeleeEffectComponent(0.5f, player, 100f));
		meleeEffect.addComponent(new AnimatorComponent());
	}

	/**
	 * Test to verify the "no-hit" raycast result related to the issue #108 on GitHub.
	 */
	@Test
	@DisplayName("Verify structure of noHit raycast results")
	public void testRaycastReturnsNoHitStructure() {
		// Configure service to find enemy in range
		when(mockCollisionService.overlapCircle(any(Vector2D.class), anyFloat(), any(PhysicsLayer.class)))
			.thenReturn(List.of(enemy));

		RaycastHit noHitResult = RaycastHit.noHit();
		when(mockCollisionService.raycast(any(Vector2D.class), any(Vector2D.class), anyFloat(), anyList()))
			.thenReturn(noHitResult);

		MeleeEffectNode node = createMeleeEffectNode();

		callApplyDamage(node);

		// Verify the structure of a noHit result
		assertFalse(noHitResult.isHit(), "noHit result should have hit=false");
		assertNull(noHitResult.getEntity(), "noHit result should have entity=null");
		assertNull(noHitResult.getPoint(), "noHit result should have point=null");
		assertNull(noHitResult.getNormal(), "noHit result should have normal=null");
		assertEquals(0f, noHitResult.getDistance(), 0.001f, "noHit result should have distance=0");
		assertNull(noHitResult.getCollider(), "noHit result should have collider=null");
	}

	/**
	 * Test to verify that the raycast parameters are correct.
	 */
	@Test
	@DisplayName("Verify correct parameters are passed to raycast")
	public void testRaycastParameters() {
		TransformComponent effectTransform = meleeEffect.getComponent(TransformComponent.class);
		TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);

		Vector2D effectPos = new Vector2D(20, 0);
		Vector2D enemyPos = new Vector2D(100, 0);
		effectTransform.setPosition(effectPos);
		enemyTransform.setPosition(enemyPos);

		Vector2D expectedDirection = enemyPos.subtract(effectPos).normalize();
		float expectedDistance = enemyPos.subtract(effectPos).magnitude();

		// Find enemy in range
		when(mockCollisionService.overlapCircle(eq(effectPos), anyFloat(), eq(PhysicsLayer.ENEMY)))
			.thenReturn(List.of(enemy));

		// Capture raycast parameters
		ArgumentCaptor<Vector2D> originCaptor = ArgumentCaptor.forClass(Vector2D.class);
		ArgumentCaptor<Vector2D> directionCaptor = ArgumentCaptor.forClass(Vector2D.class);
		ArgumentCaptor<Float> distanceCaptor = ArgumentCaptor.forClass(Float.class);
		ArgumentCaptor<List<PhysicsLayer>> layersCaptor = ArgumentCaptor.forClass(List.class);

		when(mockCollisionService.raycast(
			originCaptor.capture(),
			directionCaptor.capture(),
			distanceCaptor.capture(),
			layersCaptor.capture()
		)).thenReturn(RaycastHit.noHit());

		MeleeEffectNode node = createMeleeEffectNode();
		callApplyDamage(node);

		// Verify raycast parameters
		Vector2D capturedOrigin = originCaptor.getValue();
		Vector2D capturedDirection = directionCaptor.getValue();
		float capturedDistance = distanceCaptor.getValue();
		List<PhysicsLayer> capturedLayers = layersCaptor.getValue();

		assertEquals(effectPos, capturedOrigin, "Origin should match effect position");
		assertEquals(expectedDirection.x(), capturedDirection.x(), 0.001,
			"Direction X should be normalized vector to enemy");
		assertEquals(expectedDirection.y(), capturedDirection.y(), 0.001,
			"Direction Y should be normalized vector to enemy");
		assertEquals(expectedDistance, capturedDistance, 0.001,
			"Distance should be distance to enemy");
		assertTrue(capturedLayers.contains(PhysicsLayer.OBSTACLE),
			"Should raycast against OBSTACLE layer");
		assertTrue(capturedLayers.contains(PhysicsLayer.ENEMY),
			"Should raycast against ENEMY layer");
	}

	/**
	 * Test to verify the behavior when a raycast hits an obstacle
	 */
	@Test
	@DisplayName("Verify ray hit detection with obstacle prevents damage")
	public void testRaycastWithObstacle() {
		// Find enemy in range
		when(mockCollisionService.overlapCircle(any(Vector2D.class), anyFloat(), eq(PhysicsLayer.ENEMY)))
			.thenReturn(List.of(enemy));

		Entity obstacle = new Entity();
		obstacle.addComponent(new TransformComponent(new Vector2D(50, 0), 0, new Vector2D(1, 1)));

		RaycastHit obstacleHit = new RaycastHit(
			true,
			obstacle,
			new Vector2D(50, 0),
			new Vector2D(1, 0),
			30,
			null
		);

		// Return obstacle hit
		when(mockCollisionService.raycast(any(Vector2D.class), any(Vector2D.class), anyFloat(), anyList()))
			.thenReturn(obstacleHit);

		MeleeEffectNode node = createMeleeEffectNode();

		// Create a Spy on WeaponDamage to verify damage is not applied
		try (MockedStatic<WeaponDamage> weaponDamageMock = mockStatic(WeaponDamage.class)) {
			callApplyDamage(node);

			// Verify damage was NOT applied to enemy
			weaponDamageMock.verify(() ->
					WeaponDamage.applyDamage(eq(enemy), anyFloat()),
				never()
			);
		}

		// Verify raycast was called with both layers
		verify(mockCollisionService).raycast(
			any(Vector2D.class),
			any(Vector2D.class),
			anyFloat(),
			Mockito.<List<PhysicsLayer>>argThat(list ->
				list.contains(PhysicsLayer.OBSTACLE) &&
					list.contains(PhysicsLayer.ENEMY)
			)
		);
	}

	/**
	 * Test to verify no raycast happens when no entities are in range
	 */
	@Test
	@DisplayName("No raycast when no entities in range")
	public void testNoOverlappedEntities() {
		when(mockCollisionService.overlapCircle(any(Vector2D.class), anyFloat(), any(PhysicsLayer.class)))
			.thenReturn(Collections.emptyList());

		MeleeEffectNode node = createMeleeEffectNode();
		callApplyDamage(node);

		verify(mockCollisionService, never()).raycast(
			any(Vector2D.class), any(Vector2D.class), anyFloat(), anyList()
		);
	}

	/**
	 * Create a MeleeEffectNode
	 */
	private MeleeEffectNode createMeleeEffectNode() {
		MeleeEffectNode node = new MeleeEffectNode();

		// Init node manually
		try {
			node.initialize(meleeEffect);

			node.transform = meleeEffect.getComponent(TransformComponent.class);
			node.meleeEffect = meleeEffect.getComponent(MeleeEffectComponent.class);
			node.animator = meleeEffect.getComponent(AnimatorComponent.class);

			return node;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create MeleeEffectNode", e);
		}
	}

	/**
	 * Call applyDamage method using reflection
	 */
	private void callApplyDamage(MeleeEffectNode node) {
		try {
			Method method = MeleeSystem.class.getDeclaredMethod("applyDamage", MeleeEffectNode.class);
			method.setAccessible(true);
			method.invoke(meleeSystem, node);
		} catch (Exception e) {
			throw new RuntimeException("Failed to call applyDamage", e);
		}
	}
}