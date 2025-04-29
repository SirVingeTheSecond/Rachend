package dk.sdu.sem.enemysystem;

import dk.sdu.sem.collision.ICollisionListener;
import dk.sdu.sem.collision.events.CollisionEnterEvent;
import dk.sdu.sem.collision.events.CollisionExitEvent;
import dk.sdu.sem.collision.events.CollisionStayEvent;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

public class EnemyCollisionListener implements IComponent, ICollisionListener {
	private static final Logging LOGGER = Logging.createLogger("EnemyCollisionListener", LoggingLevel.DEBUG);

	@Override
	public void onCollisionEnter(CollisionEnterEvent event) {
		LOGGER.debug("Enemy collision enter with: " +
				event.getOther().getID() + ", contact: " + event.getContact());
	}

	@Override
	public void onCollisionStay(CollisionStayEvent event) {

	}

	@Override
	public void onCollisionExit(CollisionExitEvent event) {
		LOGGER.debug("Player collision exit with: " + event.getOther().getID());
	}
}