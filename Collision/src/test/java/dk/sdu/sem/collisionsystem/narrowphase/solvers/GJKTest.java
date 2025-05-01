package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GJK algorithm implementation.
 */
public class GJKTest {
	private GJKSolver gjkSolver;

	@BeforeEach
	public void setUp() {
		gjkSolver = new GJKSolver();
	}

	@Test
	public void testCircleCircleCollision() {
		// Two overlapping circles
		CircleShape circle1 = new CircleShape(5.0f);
		CircleShape circle2 = new CircleShape(3.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(7, 0);

		// Circles overlap by 1 unit
		ContactPoint contact = gjkSolver.solve(circle1, pos1, circle2, pos2);

		// Check collision was detected
		assertNotNull(contact);

		// Check contact normal points from A to B (first to second circle)
		assertEquals(1.0f, contact.getNormal().x(), 0.01f);
		assertEquals(0.0f, contact.getNormal().y(), 0.01f);

		// Check penetration depth (should be 1.0)
		assertEquals(1.0f, contact.getSeparation(), 0.1f);
	}

	@Test
	public void testCircleCircleNoCollision() {
		// Two separated circles
		CircleShape circle1 = new CircleShape(5.0f);
		CircleShape circle2 = new CircleShape(3.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(10, 0);

		// Circles are separated by 2 units
		ContactPoint contact = gjkSolver.solve(circle1, pos1, circle2, pos2);

		// Check no collision was detected
		assertNull(contact);
	}

	@Test
	public void testCircleCircleTouching() {
		// Two circles touching exactly at their edges
		CircleShape circle1 = new CircleShape(5.0f);
		CircleShape circle2 = new CircleShape(3.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(8, 0); // Exactly touching (5+3=8)

		// Since we allow a small tolerance, this might register as a collision
		// with zero or near-zero penetration
		ContactPoint contact = gjkSolver.solve(circle1, pos1, circle2, pos2);

		// If collision is detected, check very small penetration
		if (contact != null) {
			assertTrue(Math.abs(contact.getSeparation()) < 0.01f);
			assertEquals(1.0f, contact.getNormal().x(), 0.01f);
			assertEquals(0.0f, contact.getNormal().y(), 0.01f);
		}
	}

	@Test
	public void testCircleCircleCompletelyContained() {
		// Small circle completely inside a larger circle
		CircleShape smallCircle = new CircleShape(2.0f);
		CircleShape largeCircle = new CircleShape(10.0f);

		Vector2D smallPos = new Vector2D(0, 0);
		Vector2D largePos = new Vector2D(0, 0);

		// Small circle is inside large circle
		ContactPoint contact = gjkSolver.solve(smallCircle, smallPos, largeCircle, largePos);

		// Check collision was detected
		assertNotNull(contact);

		// Check penetration depth (should be difference in radii)
		// Since circles are concentric (same center), it should be exactly 8.0
		assertEquals(8.0f, contact.getSeparation(), 0.1f);
	}

	@Test
	public void testCircleCirclePartiallyContained() {
		// Small circle partially inside a larger circle, not centered
		CircleShape smallCircle = new CircleShape(2.0f);
		CircleShape largeCircle = new CircleShape(6.0f);

		Vector2D smallPos = new Vector2D(5, 0); // 5 units to the right
		Vector2D largePos = new Vector2D(0, 0);

		// Small circle is partially inside large circle
		ContactPoint contact = gjkSolver.solve(smallCircle, smallPos, largeCircle, largePos);

		// Check collision was detected
		assertNotNull(contact);

		// Check normal direction (we expect the normal to consistently point from first to second shape)
		assertEquals(1.0f, contact.getNormal().x(), 0.01f);
		assertEquals(0.0f, contact.getNormal().y(), 0.01f);

		// Expected penetration: distance between centers = 5, radii sum = 8, penetration = 8 - 5 = 3
		assertEquals(3.0f, contact.getSeparation(), 0.1f);
	}

	// === BOX-BOX TESTS ===

	@Test
	public void testBoxBoxCollision() {
		// Two overlapping boxes
		BoxShape box1 = new BoxShape(10.0f, 10.0f);
		BoxShape box2 = new BoxShape(10.0f, 10.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(5, 5);

		// Boxes overlap
		ContactPoint contact = gjkSolver.solve(box1, pos1, box2, pos2);

		// Check collision was detected
		assertNotNull(contact);

		// Boxes overlap by 5 units in both x and y, but penetration should
		// be the minimum distance to push out (5.0)
		assertEquals(5.0f, contact.getSeparation(), 0.1f);

		// Normal could be either (1,0) or (0,1) depending on implementation
		// So we check that it's a unit vector (length 1)
		float normalLength = (float)Math.sqrt(
			contact.getNormal().x() * contact.getNormal().x() +
				contact.getNormal().y() * contact.getNormal().y()
		);
		assertEquals(1.0f, normalLength, 0.01f);
	}

	@Test
	public void testBoxBoxNoCollision() {
		// Two separated boxes
		BoxShape box1 = new BoxShape(5.0f, 5.0f);
		BoxShape box2 = new BoxShape(5.0f, 5.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(10, 10);

		// Boxes are separated
		ContactPoint contact = gjkSolver.solve(box1, pos1, box2, pos2);

		// Check no collision was detected
		assertNull(contact);
	}

	@Test
	public void testBoxBoxTouching() {
		// Two boxes touching exactly at their edges
		BoxShape box1 = new BoxShape(5.0f, 5.0f);
		BoxShape box2 = new BoxShape(5.0f, 5.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(10, 0); // Touching on right edge of box1 with left edge of box2

		// Since we allow a small tolerance, this might register as a collision
		// with zero or near-zero penetration
		ContactPoint contact = gjkSolver.solve(box1, pos1, box2, pos2);

		// If collision is detected, check very small penetration
		if (contact != null) {
			assertTrue(Math.abs(contact.getSeparation()) < 0.01f);
			assertEquals(1.0f, contact.getNormal().x(), 0.01f);
			assertEquals(0.0f, contact.getNormal().y(), 0.01f);
		}
	}

	@Test
	public void testBoxBoxContained() {
		// Small box completely inside a larger box
		BoxShape smallBox = new BoxShape(2.0f, 2.0f);
		BoxShape largeBox = new BoxShape(10.0f, 10.0f);

		Vector2D smallPos = new Vector2D(4, 4); // Centered in large box
		Vector2D largePos = new Vector2D(0, 0);

		// Small box is inside large box
		ContactPoint contact = gjkSolver.solve(smallBox, smallPos, largeBox, largePos);

		// Check collision was detected
		assertNotNull(contact);

		// For contained box, penetration should be minimum distance to edge
		// In this case, the small box is centered, so it's 4 units in any direction
		assertTrue(Math.abs(contact.getSeparation() - 4.0f) < 0.5f);
	}

	// === CIRCLE-BOX TESTS ===

	@Test
	public void testCircleBoxCollision() {
		// Circle and box overlapping
		CircleShape circle = new CircleShape(5.0f);
		BoxShape box = new BoxShape(10.0f, 10.0f);

		Vector2D circlePos = new Vector2D(10, 10);
		Vector2D boxPos = new Vector2D(0, 0);

		// Circle and box overlap
		ContactPoint contact = gjkSolver.solve(circle, circlePos, box, boxPos);

		// Check collision was detected
		assertNotNull(contact);

		// Normal should point from circle to box
		// In this case, could be (1,1) normalized
		float normalLength = (float)Math.sqrt(
			contact.getNormal().x() * contact.getNormal().x() +
				contact.getNormal().y() * contact.getNormal().y()
		);
		assertEquals(1.0f, normalLength, 0.01f);
	}

	@Test
	public void testCircleBoxNoCollision() {
		// Circle and box separated
		CircleShape circle = new CircleShape(4.0f);
		BoxShape box = new BoxShape(5.0f, 5.0f);

		Vector2D circlePos = new Vector2D(15, 0);
		Vector2D boxPos = new Vector2D(0, 0);

		// Circle and box are separated
		ContactPoint contact = gjkSolver.solve(circle, circlePos, box, boxPos);

		// Check no collision was detected
		assertNull(contact);
	}

	@Test
	public void testCircleBoxTouching() {
		// Circle and box touching exactly at their edges
		CircleShape circle = new CircleShape(5.0f);
		BoxShape box = new BoxShape(5.0f, 5.0f);

		Vector2D circlePos = new Vector2D(10, 0); // Circle center is 10 units right, radius 5 touches box edge
		Vector2D boxPos = new Vector2D(0, 0);

		// Since we allow a small tolerance, this might register as a collision
		// with zero or near-zero penetration
		ContactPoint contact = gjkSolver.solve(circle, circlePos, box, boxPos);

		// If collision is detected, check very small penetration
		if (contact != null) {
			assertTrue(Math.abs(contact.getSeparation()) < 0.01f);
		}
	}

	@Test
	public void testBoxContainingCircle() {
		// Circle completely inside a box
		CircleShape circle = new CircleShape(2.0f);
		BoxShape box = new BoxShape(10.0f, 10.0f);

		Vector2D circlePos = new Vector2D(5, 5); // Centered in the box
		Vector2D boxPos = new Vector2D(0, 0);

		// Circle is inside box
		ContactPoint contact = gjkSolver.solve(circle, circlePos, box, boxPos);

		// Check collision was detected
		assertNotNull(contact);

		// Penetration should be distance to closest edge minus radius
		// Center is at (5,5), closest edge is at y=10, so 10 - 5 = 5, minus radius 2 = 3
		assertTrue(Math.abs(contact.getSeparation() - 3.0f) < 0.5f);
	}

	@Test
	public void testCircleContainingBox() {
		// Box completely inside a circle
		BoxShape box = new BoxShape(2.0f, 2.0f);
		CircleShape circle = new CircleShape(10.0f);

		Vector2D boxPos = new Vector2D(0, 0);
		Vector2D circlePos = new Vector2D(0, 0); // Same center

		// Box is inside circle
		ContactPoint contact = gjkSolver.solve(box, boxPos, circle, circlePos);

		// Check collision was detected
		assertNotNull(contact);

		// For a box at center, its corner is sqrt(2) units away, and radius is 10
		// so penetration should be 10 - sqrt(2) ≈ 8.6
		assertTrue(contact.getSeparation() > 8.0f && contact.getSeparation() < 9.0f);
	}

	// === EDGE CASES AND STABILITY TESTS ===

	@Test
	public void testOffsetCollision() {
		// Test collision not aligned with axes
		CircleShape circle1 = new CircleShape(5.0f);
		CircleShape circle2 = new CircleShape(3.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(6, 6); // Diagonal placement

		ContactPoint contact = gjkSolver.solve(circle1, pos1, circle2, pos2);

		// Check collision was detected
		assertNotNull(contact);

		// Normal should point in diagonal direction
		float expectedX = (float)(1.0 / Math.sqrt(2)); // 1/√2 ≈ 0.7071
		float expectedY = expectedX; // Same for y in this case

		assertEquals(expectedX, contact.getNormal().x(), 0.01f);
		assertEquals(expectedY, contact.getNormal().y(), 0.01f);
	}

	@Test
	public void testIdenticalShapes() {
		// Two identical boxes at the same position
		BoxShape box1 = new BoxShape(5.0f, 5.0f);
		BoxShape box2 = new BoxShape(5.0f, 5.0f);

		Vector2D pos1 = new Vector2D(0, 0);
		Vector2D pos2 = new Vector2D(0, 0); // Same position

		// Complete overlap
		ContactPoint contact = gjkSolver.solve(box1, pos1, box2, pos2);

		// Check collision was detected
		assertNotNull(contact);

		// Complete overlap should have a large penetration value
		assertTrue(contact.getSeparation() >= 5.0f);

		// Normal should be unit length
		float normalLength = (float)Math.sqrt(
			contact.getNormal().x() * contact.getNormal().x() +
				contact.getNormal().y() * contact.getNormal().y()
		);
		assertEquals(1.0f, normalLength, 0.01f);
	}
}