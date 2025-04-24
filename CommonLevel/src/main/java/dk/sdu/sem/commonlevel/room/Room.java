package dk.sdu.sem.commonlevel.room;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Room {
	private Scene scene;
	private List<Entity> doors = new ArrayList<>();
	private Vector2D[] entrances = new Vector2D[4];
	private HashMap<Zone, List<Vector2D>> zones = new HashMap<>();

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

	public void addZonePosition(Zone zone, Vector2D position) {
		zones.computeIfAbsent(zone, k -> new ArrayList<>()).add(position);
	}

	/**
	 * Returns list of each zone position. Or an empty list if none
	 */
	public List<Vector2D> getZonePositions(Zone zone) {
		List<Vector2D> positions = zones.get(zone);
		if (positions == null)
			positions = new ArrayList<>();

		return positions;
	}
}
