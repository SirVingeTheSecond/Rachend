package dk.sdu.sem.commonsystem;

public class Difficulty {
	private static int level = 0;

	public static void increaseLevel() {
		level++;
	}

	public static int getLevel() {
		return level;
	}
}
