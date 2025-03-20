package dk.sdu.sem.playersystem;

import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
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
		Set<PlayerNode> playerNodes = SceneManager.getInstance()
			.getActiveScene()
			.getNodeManager()
			.getNodes(PlayerNode.class);

		if (playerNodes.isEmpty()) {
			return;
		}

		setMovementAxis();

		// Apply to all player entities
		for (PlayerNode node : playerNodes) {
			handleMovement(node.transform, node.player, horizontalMovement, verticalMovement);
		}
	}

	/**
	 * Processes input and converts key states to movement values
	 */
	private void setMovementAxis() {
		int leftMove = Input.getKey(Key.LEFT) ? -1 : 0;
		int rightMove = Input.getKey(Key.RIGHT) ? 1 : 0;
		int downMove = Input.getKey(Key.DOWN) ? -1 : 0;
		int upMove = Input.getKey(Key.UP) ? 1 : 0;

		horizontalMovement = leftMove + rightMove;
		verticalMovement = upMove + downMove;
	}

	/**
	 * Applies movement to a transform component based on input and player properties
	 */
	private void handleMovement(TransformComponent transform, PlayerComponent player, float xMove, float yMove) {
		if (xMove == 0 && yMove == 0) return;

		Vector2D moveVector = new Vector2D(xMove, yMove).normalize();
		Vector2D currentPosition = transform.getPosition();

		float moveSpeed = player.getMoveSpeed();

		Vector2D newPosition = new Vector2D(
			currentPosition.getX() + (moveVector.getX() * moveSpeed),
			currentPosition.getY() + (moveVector.getY() * moveSpeed)
		);

		transform.setPosition(newPosition);
	}
}