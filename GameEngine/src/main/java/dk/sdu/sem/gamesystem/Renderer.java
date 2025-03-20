package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.data.RenderNode;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import javafx.scene.canvas.GraphicsContext;

import java.util.Set;

public class Renderer {
	private final GraphicsContext gc;

	public Renderer(GraphicsContext gc) {
		this.gc = gc;
		Entity entity = new Entity();
		entity.addComponent(TransformComponent.class,
			new TransformComponent(new Vector2D(200,200), 0, new Vector2D(1,
				1)));
		SceneManager.getInstance().getActiveScene().addEntity(entity);
	}


	public void render() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		Set<RenderNode> nodes =
			SceneManager.getInstance().getActiveScene().getNodeManager().getNodes(RenderNode.class);
		for (RenderNode currentEntity : nodes) {
			// get string from entity which is for the image to be rendered
			// Visual state.


			// draw image at correct position
//			ViewPort viewPort =
//				ViewPortFacade.getMap().get(currentEntity.renderer.getVisualState());
//			gc.drawImage( viewPort.spriteMap(),
//				viewPort.rectangle2D().getMinX()+currentEntity.transform.getPosition().getX(),
//				viewPort.rectangle2D().getMinY(),
//				viewPort.rectangle2D().getMaxX(),viewPort.rectangle2D().getMaxY());
//
//			TransformComponent transform = currentEntity.getComponent(TransformComponent.class);
//			gc.fillOval(transform.getPosition().getX() - 5, transform.getPosition().getY() - 5, 10, 10);
		}
	}
}
