package dk.sdu.sem.boss;

import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonsystem.IComponent;

import java.util.List;

public class BossComponent implements IComponent {
	private final List<Room.Zone> summonZones;

	public BossComponent(List<Room.Zone> summonZones) {
		this.summonZones = summonZones;
	}

	public List<Room.Zone> getSummonZones() {
		return summonZones;
	}
}
