package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.assets.IDisposable;
import java.util.List;
import java.util.ArrayList;

public class SpriteAnimation implements IDisposable {
	private List<Sprite> frames;
	private final double frameDuration; // Duration of each frame in seconds
	private double elapsedTime;
	private int currentFrameIndex;
	private boolean isLooping;
	private boolean isPlaying;
	private boolean isDisposed;

	public SpriteAnimation(List<Sprite> frames, double frameDuration, boolean looping) {
		this.frames = new ArrayList<>(frames); // Create defensive copy
		this.frameDuration = frameDuration;
		this.isLooping = looping;
		this.isPlaying = true;
		this.elapsedTime = 0;
		this.currentFrameIndex = 0;
		this.isDisposed = false;
	}

	public void update(double deltaTime) {
		if (!isPlaying || frames.isEmpty() || isDisposed) {
			return;
		}

		elapsedTime += deltaTime;

		if (elapsedTime >= frameDuration) {
			// Time to advance to next frame
			elapsedTime -= frameDuration;
			currentFrameIndex++;

			// Handle end of animation
			if (currentFrameIndex >= frames.size()) {
				if (isLooping) {
					currentFrameIndex = 0;
				} else {
					currentFrameIndex = frames.size() - 1;
					isPlaying = false;
				}
			}
		}
	}

	public Sprite getCurrentFrame() {
		if (frames.isEmpty() || isDisposed) {
			return null;
		}
		return frames.get(currentFrameIndex);
	}

	public void play() {
		if (!isDisposed) {
			isPlaying = true;
		}
	}

	public void pause() {
		isPlaying = false;
	}

	public void reset() {
		if (!isDisposed) {
			currentFrameIndex = 0;
			elapsedTime = 0;
			isPlaying = true;
		}
	}

	public boolean isFinished() {
		return !isLooping && !isPlaying && currentFrameIndex == frames.size() - 1;
	}

	public void setLooping(boolean looping) {
		this.isLooping = looping;
	}

	public boolean isLooping() {
		return isLooping;
	}

	/**
	 * Clean up resources when the animation is no longer needed
	 * Note: After disposal, the animation can no longer be used
	 */
	@Override
	public void dispose() {
		if (!isDisposed) {
			frames.clear();
			isDisposed = true;
			isPlaying = false;
		}
	}

	/**
	 * Check if the animation has been disposed
	 */
	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * Get the number of frames in this animation
	 */
	public int getFrameCount() {
		return frames.size();
	}

	/**
	 * Get the frame duration in seconds
	 */
	public double getFrameDuration() {
		return frameDuration;
	}
}