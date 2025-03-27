package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

/**
 * System responsible for handling player movement based on input.
 * Now updates AnimatorComponent parameters based on movement state.
 */
public class PlayerSystem implements IUpdate {
	private int horizontalMovement;
	private int verticalMovement;

	@Override
	public void update() {
		// Get all player nodes from the active scene
		Set<PlayerNode> playerNodes = NodeManager.active().getNodes(PlayerNode.class);

		if (playerNodes.isEmpty()) {
			return;
		}

		setMovementAxis();

		// Apply to all player entities
		for (PlayerNode node : playerNodes) {
			handleMovement(node, horizontalMovement, verticalMovement);
		}
	}

	/**
	 * Converts key states to movement values
	 */
	private void setMovementAxis() {
		int leftMove = Input.getKey(Key.LEFT) ? -1 : 0;
		int rightMove = Input.getKey(Key.RIGHT) ? 1 : 0;
		int downMove = Input.getKey(Key.DOWN) ? 1 : 0;
		int upMove = Input.getKey(Key.UP) ? -1 : 0;

		horizontalMovement = leftMove + rightMove;
		verticalMovement = upMove + downMove;
	}

	/**
	 * Applies movement to the physics component and updates animator parameters
	 */
	private void handleMovement(PlayerNode node, float xMove, float yMove) {
		PhysicsComponent physics = node.physicsComponent;
		PlayerComponent player = node.player;
		SpriteRendererComponent renderer = node.getEntity().getComponent(SpriteRendererComponent.class);

		boolean isInputActive = xMove != 0 || yMove != 0;

		if (renderer != null && xMove != 0) {
			renderer.setFlipX(xMove < 0);
		}

		// Skip physics update if no input
		if (!isInputActive) return;

		float moveSpeed = player.getMoveSpeed();

		// Create movement vector
		Vector2D moveVector = new Vector2D(xMove, yMove)
			.normalize()
			.scale(moveSpeed * (float)Time.getDeltaTime());

		// Apply to physics
		Vector2D velocity = physics.getVelocity();
		Vector2D newVelocity = velocity.add(moveVector);

		// Handle dash/boost
		if (Input.getKeyDown(Key.SPACE)) {
			newVelocity = newVelocity.add(
				new Vector2D(xMove, yMove)
					.normalize()
					.scale(1000)
			);
		}

		physics.setVelocity(newVelocity);
	}
}