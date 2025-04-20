package dk.sdu.sem.gamesystem.animation;

import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an animation for a specific tile in a tilemap.
 * Similar to SpriteAnimation but simplified for tiles.
 */
public class TileAnimation {
	private final List<IAssetReference<Sprite>> frameReferences;
	private final List<Float> frameDurations;
	private final boolean isLooping;

	private int currentFrameIndex;
	private float elapsedTime;

	/**
	 * Creates a new tile animation with the given frames and durations.
	 *
	 * @param frameReferences References to the sprite frames
	 * @param frameDurations Durations for each frame in seconds
	 * @param isLooping Whether the animation should loop
	 */
	public TileAnimation(List<IAssetReference<Sprite>> frameReferences, List<Float> frameDurations, boolean isLooping) {
		if (frameReferences.size() != frameDurations.size()) {
			throw new IllegalArgumentException("Number of frames must match number of durations");
		}

		this.frameReferences = new ArrayList<>(frameReferences);
		this.frameDurations = new ArrayList<>(frameDurations);
		this.isLooping = isLooping;
		this.currentFrameIndex = 0;
		this.elapsedTime = 0;
	}

	/**
	 * Updates the animation state based on elapsed time.
	 *
	 * @param deltaTime Time elapsed since last update in seconds
	 */
	public void update(float deltaTime) {
		if (frameReferences.isEmpty()) {
			return;
		}

		elapsedTime += deltaTime;

		// Get current frame duration
		float currentFrameDuration = frameDurations.get(currentFrameIndex);

		// Check if it's time to advance to the next frame
		if (elapsedTime >= currentFrameDuration) {
			elapsedTime -= currentFrameDuration;
			advanceFrame();
		}
	}

	/**
	 * Advances to the next frame in the animation.
	 */
	private void advanceFrame() {
		currentFrameIndex++;

		// Handle looping
		if (currentFrameIndex >= frameReferences.size()) {
			if (isLooping) {
				currentFrameIndex = 0;
			} else {
				currentFrameIndex = frameReferences.size() - 1;
			}
		}
	}

	/**
	 * Gets the current frame reference.
	 *
	 * @return Reference to the current frame sprite
	 */
	public IAssetReference<Sprite> getCurrentFrameReference() {
		if (frameReferences.isEmpty()) {
			return null;
		}
		return frameReferences.get(currentFrameIndex);
	}

	/**
	 * Gets all frame references for this animation.
	 *
	 * @return List of frame references
	 */
	public List<IAssetReference<Sprite>> getFrameReferences() {
		return new ArrayList<>(frameReferences);
	}

	/**
	 * Gets all frame durations for this animation.
	 *
	 * @return List of frame durations in seconds
	 */
	public List<Float> getFrameDurations() {
		return new ArrayList<>(frameDurations);
	}

	/**
	 * Checks if this animation is looping.
	 *
	 * @return True if the animation loops, false otherwise
	 */
	public boolean isLooping() {
		return isLooping;
	}

	/**
	 * Resets the animation to the first frame.
	 */
	public void reset() {
		currentFrameIndex = 0;
		elapsedTime = 0;
	}

	/**
	 * Gets the total duration of one animation cycle.
	 *
	 * @return The sum of all frame durations in seconds
	 */
	public float getTotalDuration() {
		return frameDurations.stream().reduce(0f, Float::sum);
	}

	/**
	 * Frame information for tile animations.
	 */
	public static class Frame {
		public final int tileId;
		public final int duration; // in milliseconds

		public Frame(int tileId, int duration) {
			this.tileId = tileId;
			this.duration = duration;
		}
	}
}