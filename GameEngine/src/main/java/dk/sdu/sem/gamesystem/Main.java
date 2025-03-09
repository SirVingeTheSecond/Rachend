package dk.sdu.sem.gamesystem;


public class Main {
	private static long deltaTime = 0;

	public static long getDeltatime() {
		return deltaTime;
	}

	public static void main(String[] args) {

		while (true){
			// System.nanoTime depends on os timekeeping, nanoTime depends on the time since jvm init
 			deltaTime = deltaTime - System.nanoTime();

		}
	}
}