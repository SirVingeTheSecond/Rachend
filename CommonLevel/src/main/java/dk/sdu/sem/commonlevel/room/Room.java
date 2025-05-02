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
	private HashMap<ZoneType, List<Zone>> zones = new HashMap<>();

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

	public void addZone(ZoneType zoneType, Zone zone) {
		zones.computeIfAbsent(zoneType, k -> new ArrayList<>()).add(zone);

		if (zoneType == ZoneType.NORTH_ENTRANCE)
			entrances[0] = zone.position;
		else if (zoneType == ZoneType.EAST_ENTRANCE)
			entrances[1] = zone.position;
		else if (zoneType == ZoneType.SOUTH_ENTRANCE)
			entrances[2] = zone.position;
		else if (zoneType == ZoneType.WEST_ENTRANCE)
			entrances[3] = zone.position;
	}

	/**
	 * Returns list of each zone position. Or an empty list if none
	 */
	public List<Zone> getZones(ZoneType zone) {
		List<Zone> positions = zones.get(zone);
		if (positions == null)
			positions = new ArrayList<>();

		return positions;
	}

    public Vector2D[] getEntrances() {
		return entrances;
    }

    public static class Zone {
		private String name;
		private Vector2D position;
		private float height;
		private float width;

		public Zone(String name, Vector2D position, float height, float width) {
			this.name = name;
			this.position = position;
			this.height = height;
			this.width = width;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Vector2D getPosition() {
			return position;
		}

		public void setPosition(Vector2D position) {
			this.position = position;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			this.height = height;
		}

		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}
	}
}
