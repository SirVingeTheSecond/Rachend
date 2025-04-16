package dk.sdu.sem.gamesystem.rendering;

import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.assets.IDisposable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;

public class Sprite implements IDisposable {
	private Image image;
	private final Rectangle2D sourceRect;
	private final String name;
	private boolean isDisposed;

	public Sprite(String name, Image image) {
		this.name = name;
		this.image = image;
		this.sourceRect = new Rectangle2D(0, 0, image.getWidth(), image.getHeight());
		this.isDisposed = false;
	}

	public Sprite(String name, Image image, double x, double y, double width, double height) {
		this.name = name;
		this.image = image;
		this.sourceRect = new Rectangle2D(x, y, width, height);
		this.isDisposed = false;
	}

	public Image getImage() {
		return image;
	}

	public Rectangle2D getSourceRect() {
		return sourceRect;
	}

	public String getName() {
		return name;
	}

	public void draw(GraphicsContext gc, double x, double y) {
		draw(gc, x, y, sourceRect.getWidth(), sourceRect.getHeight(), 0, false, false);
	}

	public void draw(GraphicsContext gc, double x, double y, double width, double height, double rotation) {
		draw(gc, x, y, width, height, rotation, false, false);
	}

	public void draw(GraphicsContext gc, double x, double y, double width, double height, double rotation, boolean flipX, boolean flipY) {
		if (isDisposed || image == null) {
			return;
		}

		gc.save();

		double pivotX = x + width / 2;
		double pivotY = y + height / 2;

		gc.translate(pivotX, pivotY);

		gc.rotate(Math.toDegrees(rotation));
		gc.scale(flipX ? -1 : 1, flipY ? -1 : 1);

		// Normal rendering
		gc.drawImage(
			image,
			sourceRect.getMinX(), sourceRect.getMinY(),
			sourceRect.getWidth(), sourceRect.getHeight(),
			-width / 2, -height / 2, width, height
		);

		gc.restore();
	}

	/**
	 * Clean up resources when the sprite is no longer needed
	 */
	@Override
	public void dispose() {
		if (!isDisposed) {
			// Allow the image reference to be garbage collected
			image = null;
			isDisposed = true;
		}
	}

	/**
	 * Check if the sprite has been disposed
	 */
	@Override
	public boolean isDisposed() {
		return isDisposed;
	}
}