package dk.sdu.sem.props;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.sem.collision.components.BoxColliderComponent;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.components.CollisionStateComponent;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.shapes.Bounds;
import dk.sdu.sem.collision.shapes.BoxShape;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.commonitem.ItemDropComponent;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PropFactory {
	private static final List<Prop> props;

	private static final Random random = new Random();

	static {
		ObjectMapper mapper = new ObjectMapper();
		try {
				props = mapper.readValue(PropFactory.class.getResource("/props.json"), new TypeReference<>() {
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Entity> createProps(Bounds zoneBounds, int count) {
		float minX = zoneBounds.getMinX();
		float minY = zoneBounds.getMinY();
		float width = zoneBounds.getWidth();
		float height = zoneBounds.getHeight();

		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Prop prop = props.get(random.nextInt(props.size()));
			float scale = randomFloat(prop.minSize, prop.maxSize);

			//Scale down bounds to spawn props entirely inside bounds
			Bounds collisionBounds = prop.collisionShape.getBounds();
			Bounds corrected = new Bounds(
					minX + (collisionBounds.getWidth() / 2f) * scale,
					minY + (collisionBounds.getHeight() / 2f) * scale,
					width - collisionBounds.getWidth() * scale,
					height - collisionBounds.getHeight() * scale
			);
			//If invalid bounds, then there is nowhere to spawn prop
			if (corrected.getMinX() > corrected.getMaxX() || corrected.getMinY() > corrected.getMaxY() )
				continue;

			float x = randomFloat(corrected.getMinX(), corrected.getMaxX());
			float y = randomFloat(corrected.getMinY(), corrected.getMaxY());

			Entity entity = createProp(prop, new Vector2D(x,y), scale);
			entities.add(entity);
		}
		return entities;
	}

	private static float randomFloat(float min, float max) {
		return min + random.nextFloat() * (max - min);
	}

	private static Entity createProp(Prop prop, Vector2D position, float scale) {
		Entity entity = new Entity();

		entity.addComponent(new TransformComponent(position, 0, new Vector2D(scale, scale)));
		entity.addComponent(new PhysicsComponent(5, 1));

		if (prop.collisionShape instanceof CircleShape) {
			entity.addComponent(new CircleColliderComponent(entity, ((CircleShape) prop.collisionShape).getRadius() * scale, PhysicsLayer.OBSTACLE));
		} else if (prop.collisionShape instanceof BoxShape box) {
			entity.addComponent(new BoxColliderComponent(
					entity,
					new Vector2D(
							-(box.getWidth() / 2f) * scale,
							-(box.getHeight() / 2f) * scale
					),
					box.getWidth() * scale,
					box.getHeight() * scale)
			);
		}

		entity.addComponent(new CollisionStateComponent());
		StatsComponent stats = new StatsComponent();
		stats.setMaxHealth(1);
		stats.setCurrentHealth(1);
		entity.addComponent(stats);

		SpriteRendererComponent renderer = new SpriteRendererComponent(
			prop.getSpriteReference(),
			GameConstants.LAYER_OBJECTS
		);
		entity.addComponent(renderer);

		//Breakable
		entity.addComponent(new PropBreakComponent(prop.getBrokenSpriteReference()));

		//Item drop
		entity.addComponent(new ItemDropComponent("prop", 0.05f));

		return entity;
	}
}
