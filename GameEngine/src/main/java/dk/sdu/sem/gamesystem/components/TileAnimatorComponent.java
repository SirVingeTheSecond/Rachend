package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.animation.TileAnimation;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Component that stores animation data for tiles in a tilemap.
 */
public class TileAnimatorComponent implements IComponent {
	// Maps tile IDs to their animations
	private final Map<Integer, TileAnimation> tileAnimations = new HashMap<>();

	/**
	 * Adds an animation for a specific tile ID.
	 *
	 * @param tileId The tile ID to animate
	 * @param animation The animation to use
	 */
	public void addTileAnimation(int tileId, TileAnimation animation) {
		tileAnimations.put(tileId, animation);
	}

	/**
	 * Gets the animation for a tile ID.
	 *
	 * @param tileId The tile ID
	 * @return The animation, or null if no animation exists for this tile
	 */
	public TileAnimation getAnimationForTile(int tileId) {
		return tileAnimations.get(tileId);
	}

	/**
	 * Checks if a tile ID has an animation.
	 *
	 * @param tileId The tile ID
	 * @return True if the tile has an animation, false otherwise
	 */
	public boolean hasTileAnimation(int tileId) {
		return tileAnimations.containsKey(tileId);
	}

	/**
	 * Gets all tile IDs that have animations.
	 *
	 * @return A set of tile IDs with animations
	 */
	public Set<Integer> getAnimatedTileIds() {
		return tileAnimations.keySet();
	}

	/**
	 * Gets the current frame sprite for a tile ID.
	 *
	 * @param tileId The tile ID
	 * @return The current frame sprite, or null if not found
	 */
	public Sprite getCurrentFrameSprite(int tileId) {
		TileAnimation animation = tileAnimations.get(tileId);
		if (animation != null) {
			var reference = animation.getCurrentFrameReference();
			if (reference != null) {
				return AssetManager.getInstance().resolveSprite(reference);
			}
		}
		return null;
	}

	/**
	 * Updates the animation time for a specific tile.
	 *
	 * @param tileId The tile ID to update
	 * @param deltaTime The time elapsed since the last update in seconds
	 */
	public void updateAnimationTime(int tileId, float deltaTime) {
		TileAnimation animation = tileAnimations.get(tileId);
		if (animation != null) {
			animation.update(deltaTime);
		}
	}

	/**
	 * Resets all animations to their first frame.
	 */
	public void resetAllAnimations() {
		for (TileAnimation animation : tileAnimations.values()) {
			animation.reset();
		}
	}

	/**
	 * Gets the number of animated tiles.
	 *
	 * @return The number of animated tiles
	 */
	public int getAnimatedTileCount() {
		return tileAnimations.size();
	}
}