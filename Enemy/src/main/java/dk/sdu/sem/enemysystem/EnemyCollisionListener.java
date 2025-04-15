package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.events.CollisionEnterEvent;
import dk.sdu.sem.collision.events.CollisionExitEvent;
import dk.sdu.sem.collision.events.CollisionStayEvent;
import dk.sdu.sem.commonsystem.IComponent;

public class EnemyCollisionListener implements IComponent, ICollisionListener {
	private static final boolean DEBUG = true;

	@Override
	public void onCollisionEnter(CollisionEnterEvent event) {
		if (DEBUG) {
			System.out.println("Enemy collision enter with: " +
				event.getOther().getID() + ", contact: " + event.getContact());
		}
	}

	@Override
	public void onCollisionStay(CollisionStayEvent event) {

	}

	@Override
	public void onCollisionExit(CollisionExitEvent event) {
		if (DEBUG) {
			System.out.println("Player collision exit with: " + event.getOther().getID());
		}
	}
}