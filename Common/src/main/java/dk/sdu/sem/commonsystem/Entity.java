package dk.sdu.sem.commonsystem;

import java.util.UUID;

public class Entity {

	public final UUID iD = UUID.randomUUID();

	private float[] coordinates;

	private float x;
	private float y;

	public String getID(){
		return iD.toString();
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setCoordinates(float xCoordinate, float yCoordinate) {
		coordinates = new float[] {xCoordinate, yCoordinate};
	}

	public float[] getCoordinates() {
		return coordinates;
	}
}
