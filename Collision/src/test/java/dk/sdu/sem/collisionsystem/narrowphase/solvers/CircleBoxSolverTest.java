package dk.sdu.sem.collisionsystem.narrowphase.solvers;

import dk.sdu.sem.collision.data.ContactPoint;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonsystem.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircleBoxSolverTest {
	GJKSolver solver = new GJKSolver();

	@Test
	void testInsideSolve() {
		BoxShape boxA = new BoxShape(10, 10);
		CircleShape circleA = new CircleShape(2);


	/*
	XXXXXXXXXXXXXXXXXXXXX
	X                   X
	X        XXX        X
	X       X   X       X
	X      X     X      X
	X       X   X       X
	X        XXX        X
	X                   X
	X                   X
	X                   X
	X                   X
	XXXXXXXXXXXXXXXXXXXXX
	*/
		ContactPoint point = solver.solve(circleA, new Vector2D(0,-4), boxA, new Vector2D(-5,-5));
		System.out.println(point.getSeparation());
		System.out.println(point.getPoint());

		//Expect normal going up
		assertEquals(new Vector2D(0,1), point.getNormal());

		point = solver.solve(circleA, new Vector2D(0,4), boxA, new Vector2D(-5,-5));
		System.out.println(point.getSeparation());
		System.out.println(point.getPoint());

		//Expect normal going down
		assertEquals(new Vector2D(0,-1), point.getNormal());

		point = solver.solve(circleA, new Vector2D(4,0), boxA, new Vector2D(-5,-5));
		System.out.println(point.getSeparation());
		System.out.println(point.getPoint());

		//Expect normal going right
		assertEquals(new Vector2D(-1,0), point.getNormal());

		point = solver.solve(circleA, new Vector2D(-4,0), boxA, new Vector2D(-5,-5));
		System.out.println(point.getSeparation());
		System.out.println(point.getPoint());

		//Expect normal going left
		assertEquals(new Vector2D(1,0), point.getNormal());
	}
}