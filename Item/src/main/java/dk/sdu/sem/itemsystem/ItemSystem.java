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

	private static final float FLOAT_AMPLITUDE = 0.01f; // How high items float
	private static final float FLOAT_SPEED = 1.0f; // Speed of float animation

	@Override
	public void update() {
		Set<ItemNode> itemNodes = NodeManager.active().getNodes(ItemNode.class);

		for (ItemNode itemNode : itemNodes) {
			// Skip items that are still in drop animation
			ItemDropAnimationComponent dropAnim = itemNode.getEntity().getComponent(ItemDropAnimationComponent.class);
			if (dropAnim != null && dropAnim.isAnimating()) {
				continue;
			}

			// Get base position (either from saved resting position or current)
			Vector2D basePosition;
			if (dropAnim != null && dropAnim.getRestingPosition() != null) {
				basePosition = dropAnim.getRestingPosition();
			} else {
				// For items that never had a drop animation
				basePosition = itemNode.transform.getPosition();
			}

			// Calculate float offset
			float offset = (float) Math.sin(Time.getTime() * FLOAT_SPEED) * FLOAT_AMPLITUDE;

			// Set position using the base position plus offset
			Vector2D newPosition = new Vector2D(basePosition.x(), basePosition.y() + offset);
			itemNode.transform.setPosition(newPosition);
		}
	}
}