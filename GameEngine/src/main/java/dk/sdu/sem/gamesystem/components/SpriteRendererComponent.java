package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.AssetManager;
import dk.sdu.sem.gamesystem.assets.SpriteReference;
import dk.sdu.sem.gamesystem.assets.AnimationReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;

/**
 * Component that renders a sprite for an entity.
 * Uses asset references instead of direct sprite instances.
 */
public class SpriteRendererComponent implements IComponent {
	// Asset references (serialized and shared between modules perhaps?)
	private SpriteReference spriteRef;
	private AnimationReference currentAnimationRef;

	// Runtime instances (loaded from AssetManager)
	private transient Sprite sprite;
	private transient SpriteAnimation currentAnimation;

	// Properties
	private int renderLayer = 0;
	private boolean flipX = false;
	private boolean flipY = false;
	private boolean isVisible = true;

	public SpriteRendererComponent() {
	}

	/**
	 * Creates a sprite renderer with the given sprite reference.
	 */
	public SpriteRendererComponent(SpriteReference spriteRef) {
		setSpriteReference(spriteRef);
	}

	/**
	 * Sets the sprite reference and loads the sprite.
	 * Note: If an animation is active, the sprite will be overridden by the animation frames.
	 */
	public void setSpriteReference(SpriteReference spriteRef) {
		this.spriteRef = spriteRef;
		if (spriteRef != null) {
			this.sprite = AssetManager.getInstance().getAsset(spriteRef);
		} else {
			this.sprite = null;
		}
	}

	/**
	 * Gets the sprite reference.
	 */
	public SpriteReference getSpriteReference() {
		return spriteRef;
	}

	/**
	 * Gets the currently displayed sprite.
	 * This may be from a direct sprite reference or from the current frame of an animation.
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Sets the sprite directly.
	 * This is primarily for internal use by the animation system.
	 * For external use, prefer setSpriteReference().
	 */
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	/**
	 * Sets the animation reference and loads the animation.
	 * When an animation is set, it takes precedence over the static sprite.
	 */
	public void setAnimationReference(AnimationReference animRef) {
		this.currentAnimationRef = animRef;
		if (animRef != null) {
			this.currentAnimation = AssetManager.getInstance().getAsset(animRef);
			if (this.currentAnimation != null) {
				this.currentAnimation.reset();
				// Set the initial sprite from the animation's first frame
				if (this.currentAnimation.getCurrentFrame() != null) {
					this.sprite = this.currentAnimation.getCurrentFrame();
				}
			}
		} else {
			this.currentAnimation = null;
			// Revert to static sprite if animation is removed
			if (spriteRef != null) {
				this.sprite = AssetManager.getInstance().getAsset(spriteRef);
			}
		}
	}

	/**
	 * Gets the animation reference.
	 */
	public AnimationReference getAnimationReference() {
		return currentAnimationRef;
	}

	/**
	 * Gets the current animation instance.
	 */
	public SpriteAnimation getCurrentAnimation() {
		return currentAnimation;
	}

	/**
	 * Clears any active animation and reverts to the static sprite.
	 */
	public void clearAnimation() {
		this.currentAnimation = null;
		this.currentAnimationRef = null;
		if (spriteRef != null) {
			this.sprite = AssetManager.getInstance().getAsset(spriteRef);
		}
	}

	// Standard getters and setters
	public int getRenderLayer() {
		return renderLayer;
	}

	public void setRenderLayer(int renderLayer) {
		this.renderLayer = renderLayer;
	}

	public boolean isFlipX() {
		return flipX;
	}

	public void setFlipX(boolean flipX) {
		this.flipX = flipX;
	}

	public boolean isFlipY() {
		return flipY;
	}

	public void setFlipY(boolean flipY) {
		this.flipY = flipY;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}
}