package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.assets.IDisposable;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteMapTileReference;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents an animation consisting of sprite frames.
 */
public class SpriteAnimation implements IDisposable {
	// Store only references to sprites - no direct sprite instances
	private final List<IAssetReference<Sprite>> frameReferences;
	private final double frameDuration; // Duration of each frame in seconds

	private double elapsedTime;
	private int currentFrameIndex;
	private boolean isLooping;
	private boolean isPlaying;
	private boolean isDisposed;

	/**
	 * Creates an animation from sprite references.
	 * This is the only supported way to create animations.
	 */
	public SpriteAnimation(List<IAssetReference<Sprite>> frameReferences, double frameDuration, boolean looping) {
		this.frameReferences = new ArrayList<>(frameReferences); // Create defensive copy
		this.frameDuration = frameDuration;
		this.isLooping = looping;
		this.isPlaying = true;
		this.elapsedTime = 0;
		this.currentFrameIndex = 0;
		this.isDisposed = false;
	}

	/**
	 * Updates the animation state based on elapsed time.
	 *
	 * @param deltaTime Time elapsed since last update in seconds
	 */
	public void update(double deltaTime) {
		if (!isPlaying || frameReferences.isEmpty() || isDisposed) {
			return;
		}

		elapsedTime += deltaTime;

		if (elapsedTime >= frameDuration) {
			// Time to advance to next frame
			elapsedTime -= frameDuration;
			currentFrameIndex++;

			// Handle end of animation
			if (currentFrameIndex >= frameReferences.size()) {
				if (isLooping) {
					currentFrameIndex = 0;
				} else {
					currentFrameIndex = frameReferences.size() - 1;
					isPlaying = false;
				}
			}
		}
	}

	/**
	 * Gets the current frame sprite by resolving its reference.
	 * This is primarily for rendering and should not be used to modify the sprite.
	 */
	public Sprite getCurrentFrame() {
		if (frameReferences.isEmpty() || isDisposed) {
			return null;
		}

		IAssetReference<Sprite> reference = frameReferences.get(currentFrameIndex);

		// Handle sprite map tile references specially
		if (reference instanceof SpriteMapTileReference) {
			return ((SpriteMapTileReference) reference).resolveSprite();
		}

		// For regular references, use the AssetManager
		return AssetManager.getInstance().getAsset(reference);
	}

	/**
	 * Gets the current frame's reference.
	 * This is the preferred way to access the current frame.
	 */
	public IAssetReference<Sprite> getCurrentFrameReference() {
		if (frameReferences.isEmpty() || isDisposed) {
			return null;
		}
		return frameReferences.get(currentFrameIndex);
	}

	/**
	 * Sets the current frame index.
	 */
	public void setCurrentFrameIndex(int frameIndex) {
		if (frameIndex >= 0 && frameIndex < frameReferences.size()) {
			this.currentFrameIndex = frameIndex;
		}
	}

	/**
	 * Starts playing the animation.
	 */
	public void play() {
		if (!isDisposed) {
			isPlaying = true;
		}
	}

	/**
	 * Pauses the animation.
	 */
	public void pause() {
		isPlaying = false;
	}

	/**
	 * Resets the animation to the first frame.
	 */
	public void reset() {
		if (!isDisposed) {
			currentFrameIndex = 0;
			elapsedTime = 0;
			isPlaying = true;
		}
	}

	/**
	 * Checks if a non-looping animation has finished.
	 */
	public boolean isFinished() {
		return !isLooping && !isPlaying && currentFrameIndex == frameReferences.size() - 1;
	}

	/**
	 * Sets whether the animation should loop.
	 */
	public void setLooping(boolean looping) {
		this.isLooping = looping;
	}

	/**
	 * Gets whether the animation is looping.
	 */
	public boolean isLooping() {
		return isLooping;
	}

	/**
	 * Clean up resources when the animation is no longer needed.
	 */
	@Override
	public void dispose() {
		if (!isDisposed) {
			frameReferences.clear();
			isDisposed = true;
			isPlaying = false;
		}
	}

	/**
	 * Check if the animation has been disposed.
	 */
	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * Get the number of frames in this animation.
	 */
	public int getFrameCount() {
		return frameReferences.size();
	}

	/**
	 * Get the frame duration in seconds.
	 */
	public double getFrameDuration() {
		return frameDuration;
	}

	/**
	 * Get the list of frame references.
	 */
	public List<IAssetReference<Sprite>> getFrameReferences() {
		// Return a copy
		return new ArrayList<>(frameReferences);
	}
}