package dk.sdu.sem.itemsystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;

import java.util.Set;

/**
 * System for processing items.
 */
public class ItemSystem implements IUpdate {

	private static final float FLOAT_AMPLITUDE = 0.01f;  // How high items float
	private static final float FLOAT_SPEED = 1.0f;      // Speed of float animation

	@Override
	public void update() {
		Set<ItemNode> itemNodes = NodeManager.active().getNodes(ItemNode.class);

		for (ItemNode itemNode : itemNodes) {
			// Make items bob up and down using sin wave
			Vector2D position = itemNode.transform.getPosition();

			// Offset Y position with sin wave based on time
			float offset = (float) Math.sin(Time.getTime() * FLOAT_SPEED) * FLOAT_AMPLITUDE;

			Vector2D newPosition = new Vector2D(position.getX(), position.getY() + offset);
			itemNode.transform.setPosition(newPosition);
		}
	}
}