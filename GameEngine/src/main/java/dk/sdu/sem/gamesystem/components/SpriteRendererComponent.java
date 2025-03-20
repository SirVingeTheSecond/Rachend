package dk.sdu.sem.gamesystem.components;

import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.gamesystem.rendering.SpriteAnimation;
import javafx.scene.paint.Color;

public class SpriteRendererComponent implements IComponent {
	private Sprite sprite;
	private int renderLayer = 0;
	private boolean flipX = false;
	private boolean flipY = false;
	private Color tint = Color.WHITE;
	private SpriteAnimation currentAnimation;
	private boolean isVisible = true;

	public SpriteRendererComponent() {
	}

	public SpriteRendererComponent(Sprite sprite) {
		this.sprite = sprite;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

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

	public Color getTint() {
		return tint;
	}

	public void setTint(Color tint) {
		this.tint = tint;
	}

	public SpriteAnimation getCurrentAnimation() {
		return currentAnimation;
	}

	public void setCurrentAnimation(SpriteAnimation animation) {
		this.currentAnimation = animation;
		if (animation != null) {
			animation.reset();
		}
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}
}