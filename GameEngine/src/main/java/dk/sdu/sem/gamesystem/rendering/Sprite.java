package dk.sdu.sem.gamesystem.rendering;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Sprite {
	private final Image image;
	private final Rectangle2D sourceRect;
	private final String name;

	public Sprite(String name, Image image) {
		this.name = name;
		this.image = image;
		this.sourceRect = new Rectangle2D(0, 0, image.getWidth(), image.getHeight());
	}

	public Sprite(String name, Image image, double x, double y, double width, double height) {
		this.name = name;
		this.image = image;
		this.sourceRect = new Rectangle2D(x, y, width, height);
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
		draw(gc, x, y, sourceRect.getWidth(), sourceRect.getHeight(), false, false);
	}

	public void draw(GraphicsContext gc, double x, double y, double width, double height) {
		draw(gc, x, y, width, height, false, false);
	}

	public void draw(GraphicsContext gc, double x, double y, double width, double height, boolean flipX, boolean flipY) {
		if (flipX || flipY) {
			// For flipped rendering, we need to use transforms
			gc.save();
			gc.translate(x + (flipX ? width : 0), y + (flipY ? height : 0));
			gc.scale(flipX ? -1 : 1, flipY ? -1 : 1);

			gc.drawImage(
				image,
				sourceRect.getMinX(), sourceRect.getMinY(),
				sourceRect.getWidth(), sourceRect.getHeight(),
				0, 0, width, height
			);

			gc.restore();
		} else {
			// Normal rendering
			gc.drawImage(
				image,
				sourceRect.getMinX(), sourceRect.getMinY(),
				sourceRect.getWidth(), sourceRect.getHeight(),
				x, y, width, height
			);
		}
	}
}