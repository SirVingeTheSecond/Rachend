import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.roomsystem.RoomSystem;

module Room {
	requires GameEngine;

	provides IStart with RoomSystem;
}
