package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import javafx.scene.canvas.GraphicsContext;

public class Renderer {
	private final GraphicsContext gc;

	public Renderer(GraphicsContext gc) {
		this.gc = gc;
	}

	public void render() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		for (Entity entity : SceneManager.getInstance().getActiveScene().getEntities()) {
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			gc.fillOval(transform.getPosition().getX() - 5, transform.getPosition().getY() - 5, 10, 10);
		}
	}
}
