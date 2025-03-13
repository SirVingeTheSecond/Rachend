package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;

public class PlayerControl {

	//transform refers to the transform component attached to the player
	public TransformComponent transform;

	int horizontalMovement;
	int verticalMovement;

	public void Update(){
		SetMovementAxis();
		Movement(horizontalMovement, verticalMovement);
	}

	//this should maybe be moved into input?
	public void SetMovementAxis(){
		//Changing booleans to integers
		int leftMove = Input.getKey(Key.LEFT) ? -1 : 0;
		int rightMove = Input.getKey(Key.RIGHT) ? 1 : 0;
		int downMove = Input.getKey(Key.DOWN) ? -1 : 0;
		int upMove = Input.getKey(Key.UP) ? 1 : 0;

		//combining integers to horizontal and vertical movement
		horizontalMovement = leftMove + rightMove;
		verticalMovement = upMove + downMove;
	}

	//movespeed of 1 results in the player moving the normal speed
	private float moveSpeed = 1.0f;

	public void SetPlayer(TransformComponent playerTransform) {
		//needs to be changed to fetch the actual player, should only be run once on load
		transform = playerTransform;
	}

	public void Movement(float xMove, float yMove) {
		if(xMove == 0 && yMove == 0) return;

		Vector2D tempVector = new Vector2D(xMove * moveSpeed, yMove * moveSpeed).normalize();

		//sets the current position of the player to it's own position, plus the input * the Movement multiplier
		transform.setPosition(new Vector2D(transform.getPosition().getX() + tempVector.getX(),transform.getPosition().getY() + tempVector.getY()));
	}
}
