package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;

public class Renderer {
	Image image = new Image("floor.png");
	private final GraphicsContext gc;

	public Renderer(GraphicsContext gc) {

		this.gc = gc;
	}

	public void render() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		Random rand = new Random(1);

		int size = 32;
		for (int w = 0; w < 800; w+=size) {
			for (int h = 0; h < 600; h+=size) {
				gc.drawImage(image,  Math.floor(rand.nextDouble() * 3) * 16, Math.floor(rand.nextDouble() * 4) * 16, 16, 16, w, h, size, size);
			}
		}

		for (Entity entity : SceneManager.getInstance().getActiveScene().getEntities()) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			gc.fillOval(transform.getPosition().getX() - 5, transform.getPosition().getY() - 5, 10, 10);


		}

		System.out.println("FPS: " + 1 / Time.getDeltaTime());
	}
}
