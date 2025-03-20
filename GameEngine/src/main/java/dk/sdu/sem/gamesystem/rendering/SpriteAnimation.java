package dk.sdu.sem.gamesystem.rendering;

import java.util.List;

public class SpriteAnimation {
	private final List<Sprite> frames;
	private final double frameDuration; // Duration of each frame in seconds
	private double elapsedTime;
	private int currentFrameIndex;
	private boolean isLooping;
	private boolean isPlaying;

	public SpriteAnimation(List<Sprite> frames, double frameDuration, boolean looping) {
		this.frames = frames;
		this.frameDuration = frameDuration;
		this.isLooping = looping;
		this.isPlaying = true;
		this.elapsedTime = 0;
		this.currentFrameIndex = 0;
	}

	public void update(double deltaTime) {
		if (!isPlaying || frames.isEmpty()) {
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
		if (frames.isEmpty()) {
			return null;
		}
		return frames.get(currentFrameIndex);
	}

	public void play() {
		isPlaying = true;
	}

	public void pause() {
		isPlaying = false;
	}

	public void reset() {
		currentFrameIndex = 0;
		elapsedTime = 0;
		isPlaying = true;
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
}