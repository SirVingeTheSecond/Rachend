package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.player.PlayerComponent;

import java.util.Set;

/**
 * System responsible for handling player movement based on input.
 * Uses our Node pattern.
 * */
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
			handleMovement(node.physicsComponent, node.player, horizontalMovement, verticalMovement);
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
	 * Applies movement to the transform component based on input and speed
	 */
	private void handleMovement(PhysicsComponent physicsComponent, PlayerComponent player, float xMove, float yMove) {
		if (xMove == 0 && yMove == 0) return;

		float moveSpeed = player.getMoveSpeed();

		Vector2D moveVector = new Vector2D(xMove, yMove).normalize().scale(moveSpeed * (float)Time.getDeltaTime());
		Vector2D velocity = physicsComponent.getVelocity();
		Vector2D newVelocity = velocity.add(moveVector);

		if (Input.getKeyDown(Key.SPACE))
		{
			newVelocity = newVelocity.add(new Vector2D(xMove, yMove).normalize().scale(1000));
		}

		physicsComponent.setVelocity(newVelocity);
	}
}