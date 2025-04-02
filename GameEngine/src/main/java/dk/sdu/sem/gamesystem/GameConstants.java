package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Vector2D;

/**
 * Constants used throughout the game.
 */
// Could be relocated to Common but should others now about these?
public final class GameConstants {
	// Tile dimensions
	public static final int TILE_SIZE = 24;

	//World Dimensions
	public static final Vector2D WORLD_SIZE = new Vector2D(25, 19);

	// Animation constants
	public static final double DEFAULT_FRAME_DURATION = 0.15; // seconds per frame

	// Layer constants (for rendering order)
	public static final int LAYER_FLOOR = 0;
	public static final int LAYER_BACKGROUND = 10;
	public static final int LAYER_MIDGROUND = 20;
	public static final int LAYER_CHARACTERS = 30;
	public static final int LAYER_EFFECTS = 40;
	public static final int LAYER_FOREGROUND = 40;
	public static final int LAYER_UI = 100;
}