package dk.sdu.sem.levelsystem;

import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Level class contains the constructor for a level as well a methods for generating a layout.
 * "endRooms" will be saved for later selection of boss, item or shop rooms.
 * Starting room, maxRooms and minRooms can be set.
 */
public class Level {
	private static final Logging LOGGER = Logging.createLogger("Level", LoggingLevel.DEBUG);

	private boolean[][] layout;
	private List<Integer> endRooms;
	private Queue<Integer> roomQueue;
	private int roomCount;
	private final int maxRooms;
	private final int minRooms;
	private final int width;
	private final int height;
	private final int startRoom;

	/**
	 * Constructor for a level object
	 * @param minRooms minimum amount of rooms, must not be greater than maxRooms.
	 * @param maxRooms maximum amount of rooms, must be less than 1/4 of total grid size.
	 * @param width width of the layout "grid", must be at least 5.
	 * @param height height of the layout "grid", must be at least 5.
	 */
	public Level(int minRooms, int maxRooms, int width, int height) {
		this.maxRooms = maxRooms;
		this.minRooms = minRooms;
		this.width = width;
		this.height = height;
		startRoom = width*height/2 - width/2;
	}

	/**
	 * Method for generating the level layout with all rooms.
	 * A room queue is created and each neighbour is visited.
	 * Doors and rooms are created if the visit is successful.
	 * The room will be added to the endRoom list if no neighbour was created.
	 * The method will retry level creation if too small.
	 */
	public void createLayout() {
		if (minRooms > maxRooms | width < 5 | height < 5 | maxRooms > height*width/4) {
			throw new IllegalArgumentException("illegal argument for layout creation");
		}
		layout = new boolean[width*height][5];
		endRooms = new LinkedList<>();
		roomQueue = new LinkedList<>();
		roomCount = 0;
		visit(startRoom);

		while (!roomQueue.isEmpty()) {
			int i = roomQueue.poll();
			int x = i % width;
			boolean created = false;

			if(x > 1) {
				boolean left = visit(i-1);
				created |= left;
				if (left) {
					layout[i][4] = true;
					layout[i-1][2] = true;
				}
			}
			if(x < 9) {
				boolean right = visit(i+1);
				created |= right;
				if (right) {
					layout[i][2] = true;
					layout[i+1][4] = true;
				}
			}
			if(i > width*2) {
				boolean up = visit(i-width);
				created |= up;
				if (up) {
					layout[i][1] = true;
					layout[i-width][3] = true;
				}
			}
			if(i < width*height-2*width) {
				boolean down = visit(i+width);
				created |= down;
				if (down) {
					layout[i][3] = true;
					layout[i+width][1] = true;
				}
			}
			if(!created) {
				endRooms.add(i);
			}
		}
		printLayout();

		//recur if layout is too small.
		if (minRooms > roomCount) {
			createLayout();
		}
	}

	/**
	 * Method makes multiple checks to see if the given room can be created.
	 * The room will be added to the queue and layout array will be updated.
	 * @param i is the number of the room from the layout array
	 * @return True if a room is created, and false if not.
	 */
	private boolean visit(int i) {

		if (layout[i][0]) return false;

		int neighbours = ncount(i);

		if (neighbours > 1) return false;

		if (roomCount >= maxRooms) return false;

		if (Math.random() < 0.5 && i != startRoom) {
			return false;
		}

		roomQueue.add(i);
		layout[i][0] = true;
		roomCount += 1;

		return true;
	}

	/**
	 * Counts the amount of neighbours that a given room in the layout array has.
	 * @param i is the number of the room in the layout.
	 * @return the amount of neighbours of the given room.
	 */
	private int ncount(int i) {
		int neighbours = 0;
		if (layout[i-width][0]) neighbours++;
		if (layout[i-1][0]) neighbours++;
		if (layout[i+1][0]) neighbours++;
		if (layout[i+width][0]) neighbours++;
		return neighbours;
	}

	public boolean[][] getLayout() {
		return layout;
	}

	public int getStartRoom() {
		return startRoom;
	}

	public List<Integer> getEndRooms() {
		return endRooms;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	// Debug method for printing which rooms have been created
	// Endrooms and doors between rooms are also printed.
	public void printLayout() {
		int length = layout.length;
		int i = 0;

		LOGGER.debug("\nLayout of Level (brackets indicate a room is created):");
		while (i < length) {
			if (i % 10 == 0) LOGGER.println(LoggingLevel.DEBUG, "");
			LOGGER.print(LoggingLevel.DEBUG, layout[i][0] ? "["+i+"]" : " "+i+" ");
			i++;
		}

		LOGGER.debug("\n\nRooms which created no neighbours (endrooms): ");
		for (i = 0; i < endRooms.size(); i++) {
			LOGGER.print(LoggingLevel.DEBUG,endRooms.get(i) + " ");
		}

		LOGGER.debug("\n\nConnections between rooms: ");
		int j = 0;
		int row = 0;
		while (j < length) {
			if (j % 10 == 0 && j != 0) {
				LOGGER.println(LoggingLevel.DEBUG, "");
				row += 1;
				if (row % 3 != 0) {
					j -= width;
				}
			}
			switch (row % 3) {
				case 0:
					LOGGER.print(LoggingLevel.DEBUG, layout[j][1] ? " | " : "   ");
					break;
				case 1: {
					LOGGER.print(LoggingLevel.DEBUG, layout[j][4] ? "-" : " ");
					LOGGER.print(LoggingLevel.DEBUG, layout[j][0] ? "X" : " ");
					LOGGER.print(LoggingLevel.DEBUG, layout[j][2] ? "-" : " ");
					break;
				}
				case 2:
					LOGGER.print(LoggingLevel.DEBUG, layout[j][3] ? " | " : "   ");
					break;
			}
			j++;
		}
		LOGGER.print(LoggingLevel.DEBUG,"\n");
	}
}
