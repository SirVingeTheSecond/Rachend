package dk.sdu.sem.commonsystem;

public class Difficulty {
	private static int level = 5;

	public static void increaseLevel() {
		level++;
	}

	public static void setLevel(int level) {
		Difficulty.level = level;
	}

	public static int getLevel() {
		return level;
	}
}
