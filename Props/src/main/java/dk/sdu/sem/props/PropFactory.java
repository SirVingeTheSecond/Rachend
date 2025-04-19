package dk.sdu.sem.props;

import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PropFactory {
	private static List<String> propSprites = List.of(
		"vase1",
		"vase2",
		"box1"
	);
	private static Random random = new Random();


	public static List<Entity> createProps(Vector2D position) {
		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < random.nextFloat() * 10; i++) {
			Vector2D offsetPos = position.add(new Vector2D(
				(float) (((random.nextFloat() * 2) - 1) * GameConstants.TILE_SIZE / 2),
				(float) (((random.nextFloat() * 2) - 1) * GameConstants.TILE_SIZE / 2)
			));

			Entity prop = createProp(offsetPos);
			entities.add(prop);
		}
		return entities;
	}

	private static Entity createProp(Vector2D position) {
		Entity prop = new Entity();
		Vector2D scale = new Vector2D(1.0f, 1.0f).scale((float) (1 + random.nextFloat() * 0.5));
		prop.addComponent(new TransformComponent(position, 0, scale));
		prop.addComponent(new PhysicsComponent(5, 1));

		prop.addComponent(new CircleColliderComponent(prop, scale.x() * 10, PhysicsLayer.OBSTACLE));
		prop.addComponent(new CollisionStateComponent());

		String sprite = propSprites.get(random.nextInt(propSprites.size()));
		SpriteRendererComponent renderer = new SpriteRendererComponent(
			new SpriteReference(sprite + "_sprite"),
			GameConstants.LAYER_PLAYER
		);
		prop.addComponent(renderer);

		prop.addComponent(new PropBreakComponent(prop, new SpriteReference(sprite + "_broken_sprite")));

		return prop;
	}
}
