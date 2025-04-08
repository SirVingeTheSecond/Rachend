package dk.sdu.sem.commonlevel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Level {
	private final boolean[][] layout = new boolean[79][4];
	private final List<Integer> endRooms = new LinkedList<>();
	private final Queue<Integer> roomQueue = new LinkedList<>();
	private int roomCount;
	private int maxRooms;
	private int minRooms;
	private final int startRoom = 35;

	/**
	 * Creates a default level with 10 max rooms and 6 min rooms.
	 */
	public Level() {
		this.maxRooms = 10;
		this.minRooms = 6;
	}

	/**
	 * Creates a level with custom room amount allocation
	 * @param maxRooms The max amount of rooms which can be generated in the level
	 * @param minRooms The minimum amount of rooms which can be generated in the level
	 */
	public Level(int maxRooms, int minRooms) {
		this.maxRooms = maxRooms;
		this.minRooms = minRooms;
	}

	/**
	 * Create a method for generating the level layout with all rooms.
	 * Active room will be set and a list of rooms and their adjacent rooms will be created.
	 */
	public void generateLayout() {
		visit(startRoom);

		while (!roomQueue.isEmpty()) {
			int i = roomQueue.poll();
			int x = i % 10;
			boolean created = false;

			if(x > 1) created = visit(i-1);
			if(x < 9) created = visit(i+1);
			if(i > 20) created = visit(i-10);
			if(i < 70) created = visit(i+10);
			if(!created) {
				endRooms.add(i);
			}
		}

		printLayout();
	}

	private boolean visit(int i) {

		if (layout[i][0]) return false;

		int neighbours = ncount(i);

		if (neighbours > 1) return false;

		if (roomCount >= maxRooms) return false;

		if (Math.random() > 0.5 && i != startRoom) {
			return false;
		}

		roomQueue.add(i);
		layout[i][0] = true;
		roomCount += 1;

		return true;
	}

	private int ncount(int i) {
		int neighbours = 0;
		if (layout[i-10][0]) neighbours++;
		if (layout[i-1][0]) neighbours++;
		if (layout[i+1][0]) neighbours++;
		if (layout[i+10][0]) neighbours++;
		return neighbours;
	}

	// Method for printing which rooms have been created
	public void printLayout() {
		int length = layout.length;
		int i = 0;
		System.out.println("Layout of Level: X means room, O means nothing");
		while (i < length) {
			if (i % 10 == 0) System.out.println();
			System.out.print(layout[i][0] ? "[X]" : "[O]");
			i++;
		}
		for (i = 0; i < endRooms.size(); i++) {
			System.out.print(endRooms.get(i) + " ");
		}
		System.out.println(" ");
	}
}
