package dk.sdu.sem.commonlevel.room;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private Scene scene;
	private List<Entity> doors = new ArrayList<>();
	private Vector2D[] entrances = new Vector2D[4];
	private List<Vector2D> enemySpawnPoints = new ArrayList<>();

	public Room(Scene scene) {
		this.scene = scene;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public List<Entity> getDoors() {
		return doors;
	}

	public void setDoors(List<Entity> doors) {
		this.doors = doors;
	}

	public Vector2D[] getEntrances() {
		return entrances;
	}

	public void setEntrances(Vector2D[] entrances) {
		this.entrances = entrances;
	}

	public List<Vector2D> getEnemySpawnPoints() {
		return enemySpawnPoints;
	}

	public void setEnemySpawnPoints(List<Vector2D> enemySpawnPoints) {
		this.enemySpawnPoints = enemySpawnPoints;
	}
}
