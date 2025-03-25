package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.rendering.Sprite;

/**
 * Component that renders a sprite for an entity.
 */
public class SpriteRendererComponent implements IComponent {
	private Sprite sprite;
	private String spriteName;

	private int renderLayer = 0;
	private boolean flipX = false;
	private boolean flipY = false;
	private boolean visible = true;

	/**
	 * Creates an empty sprite renderer.
	 */
	public SpriteRendererComponent() {
	}

	/**
	 * Creates a sprite renderer with a sprite.
	 *
	 * @param spriteName Name of the sprite
	 */
	public SpriteRendererComponent(String spriteName) {
		setSprite(spriteName);
	}

	/**
	 * Gets the current sprite.
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Sets the sprite by name.
	 *
	 * @param spriteName Name of the sprite
	 */
	public void setSprite(String spriteName) {
		this.spriteName = spriteName;
		this.sprite = AssetFacade.loadSprite(spriteName);
	}

	/**
	 * Gets the name of the current sprite.
	 */
	public String getSpriteName() {
		return spriteName;
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