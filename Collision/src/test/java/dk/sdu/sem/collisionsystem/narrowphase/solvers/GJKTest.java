package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Vector2D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GJK algorithm implementation.
 */
public class GJKTest {
	private final GJKSolver gjkSolver = new GJKSolver();

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

		// Check contact normal points from circle2 to circle1
		assertEquals(-1.0f, contact.getNormal().x(), 0.001f);
		assertEquals(0.0f, contact.getNormal().y(), 0.001f);

		// Check penetration depth
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

		// Check penetration depth (boxes overlap by 5 units in both x and y)
		assertEquals(5.0f, contact.getSeparation(), 0.1f);
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
	public void testCompletelyContainedShapes() {
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
		assertEquals(8.0f, contact.getSeparation(), 0.1f);
	}
}