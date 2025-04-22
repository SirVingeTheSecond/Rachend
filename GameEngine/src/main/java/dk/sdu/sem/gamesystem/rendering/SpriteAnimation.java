package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.assets.IDisposable;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteMapTileReference;

import java.util.ArrayList;
import java.util.List;

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
	private boolean isReversed = false;

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

			if (isReversed) {
				currentFrameIndex--;

				// Handle beginning of animation when playing in reverse
				if (currentFrameIndex < 0) {
					if (isLooping) {
						currentFrameIndex = frameReferences.size() - 1;
					} else {
						currentFrameIndex = 0;
						isPlaying = false;
					}
				}
			} else {
				currentFrameIndex++;

				// Handle end of animation when playing forward
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
	 * Starts playing the animation in reverse.
	 */
	public void playReverse() {
		if (!isDisposed) {
			isReversed = true;
			isPlaying = true;
		}
	}

	/**
	 * Starts playing the animation in a specified direction.
	 *
	 * @param reverse True to play in reverse, false to play forward
	 */
	public void play(boolean reverse) {
		if (!isDisposed) {
			isReversed = reverse;
			isPlaying = true;
		}
	}

	/**
	 * Flips the current playback direction and ensures the animation is playing.
	 */
	public void flipDirection() {
		if (!isDisposed) {
			isReversed = !isReversed;
			isPlaying = true;
		}
	}

	/**
	 * Checks if the animation is currently playing in reverse.
	 *
	 * @return True if playing in reverse, false if playing forward
	 */
	public boolean isReversed() {
		return isReversed;
	}

	/**
	 * Pauses the animation.
	 */
	public void pause() {
		isPlaying = false;
	}

	/**
	 * Resets the animation to the first frame for forward playback,
	 * or to the last frame for reverse playback.
	 */
	public void reset() {
		if (!isDisposed) {
			currentFrameIndex = isReversed ? frameReferences.size() - 1 : 0;
			elapsedTime = 0;
			isPlaying = true;
		}
	}

	/**
	 * Resets the animation to the first frame and sets direction to forward.
	 */
	public void resetToStart() {
		if (!isDisposed) {
			currentFrameIndex = 0;
			elapsedTime = 0;
			isReversed = false;
			isPlaying = true;
		}
	}

	/**
	 * Resets the animation to the last frame and sets direction to reverse.
	 */
	public void resetToEnd() {
		if (!isDisposed) {
			currentFrameIndex = Math.max(0, frameReferences.size() - 1);
			elapsedTime = 0;
			isReversed = true;
			isPlaying = true;
		}
	}

	/**
	 * Checks if a non-looping animation has finished.
	 */
	public boolean isFinished() {
		if (isLooping) {
			return false;
		}

		if (isReversed) {
			return !isPlaying && currentFrameIndex == 0;
		} else {
			return !isPlaying && currentFrameIndex == frameReferences.size() - 1;
		}
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