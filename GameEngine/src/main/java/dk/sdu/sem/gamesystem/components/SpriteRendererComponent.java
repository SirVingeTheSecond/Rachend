package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.managers.AssetManager;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.rendering.Sprite;

/**
 * Component that renders a sprite for an entity.
 * Uses a purely reference-based approach for sprite handling.
 */
public class SpriteRendererComponent implements IComponent {
	private IAssetReference<Sprite> spriteReference;
	private Sprite cachedSprite;

	private int renderLayer = 0;
	private boolean flipX = false;
	private boolean flipY = false;
	private boolean visible = true;

	/**
	 * Creates an empty sprite renderer with no reference.
	 */
	public SpriteRendererComponent() {
	}

	/**
	 * Creates a sprite renderer with a sprite reference.
	 *
	 * @param reference The sprite reference
	 */
	public SpriteRendererComponent(IAssetReference<Sprite> reference) {
		setSprite(reference);
	}

	/**
	 * Gets the current sprite by resolving its reference.
	 * Uses caching for performance.
	 */
	public Sprite getSprite() {
		if (spriteReference == null) {
			return null;
		}

		// Use cached sprite if available
		if (cachedSprite != null) {
			return cachedSprite;
		}

		// Resolve the sprite reference
		cachedSprite = AssetManager.getInstance().resolveSprite(spriteReference);
		return cachedSprite;
	}

	/**
	 * Sets the sprite by reference.
	 * This is the only supported way to set sprites.
	 *
	 * @param reference The sprite reference
	 */
	public void setSprite(IAssetReference<Sprite> reference) {
		this.spriteReference = reference;
		this.cachedSprite = null;
	}

	/**
	 * Gets the sprite reference.
	 */
	public IAssetReference<Sprite> getSpriteReference() {
		return spriteReference;
	}

	/**
	 * Invalidates the sprite cache, forcing reference resolution on next getSprite().
	 * This should be called if the referenced sprite might have changed.
	 */
	public void invalidateCache() {
		this.cachedSprite = null;
	}

	// Property getters and setters

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
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}