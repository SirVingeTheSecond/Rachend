package dk.sdu.sem.gamesystem.input;

import java.util.EnumMap;

public class Input {
	private static final EnumMap<Key, Boolean> currentKeys = new EnumMap<>(Key.class);
	private static final EnumMap<Key, Boolean> prevKeys = new EnumMap<>(Key.class);

	static {
		for (Key key : Key.values()) {
			currentKeys.put(key, false);
			prevKeys.put(key, false);
		}
	}

	/**
	 * Returns true while the user holds down the key.
	 */
	public static boolean getKey(Key key) {
		return currentKeys.get(key);
	}

	/**
	 * Returns true during the frame the user starts pressing down the key.
	 */
	public static boolean getKeyDown(Key key) {
		return currentKeys.get(key) && !prevKeys.get(key);
	}

	/**
	 * Returns true during the frame the user stops pressing down the key.
	 */
	public static boolean getKeyUp(Key key) {
		return !currentKeys.get(key) && prevKeys.get(key);
	}

	public static void update() {
		prevKeys.putAll(currentKeys);
	}

	public static void setKeyPressed(Key key, boolean pressed) {
		currentKeys.put(key, pressed);
	}
}
