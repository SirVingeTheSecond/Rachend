package dk.sdu.sem.roomsystem;

import dk.sdu.sem.gamesystem.services.IStart;

public class RoomSystem implements IStart {
	@Override
	public void start() {
		System.out.println("DIN MOR");
		Room level = Room.generate();
		level.traverse(System.out::println);
		System.out.println(level);
	}
}
