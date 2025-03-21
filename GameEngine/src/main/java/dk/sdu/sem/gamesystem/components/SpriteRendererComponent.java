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
	 * Gets the loaded sprite instance.
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Sets the animation reference and loads the animation.
	 */
	public void setAnimationReference(AnimationReference animRef) {
		this.currentAnimationRef = animRef;
		if (animRef != null) {
			this.currentAnimation = AssetManager.getInstance().getAsset(animRef);
			if (this.currentAnimation != null) {
				this.currentAnimation.reset();
			}
		} else {
			this.currentAnimation = null;
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