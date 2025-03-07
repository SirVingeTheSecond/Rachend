package dk.sdu.sem.playersystem;

import javax.swing.text.html.parser.Entity;

public class PlayerControl {

	public Entity player;

	public void SetPlayer(Entity player) {
		//needs to be changed to fetch the actual player, should only be run once on load
		this.player = player;
	}

	public void movement(float xMove, float yMove) {
		if(xMove == 0 && yMove == 0) return;


	}
}
