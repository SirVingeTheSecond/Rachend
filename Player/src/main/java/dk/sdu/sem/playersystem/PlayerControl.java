package dk.sdu.sem.playersystem;

import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;


public class PlayerControl {

	//transform refers to the transform component attached to the player
	public TransformComponent transform;

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
